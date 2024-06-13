package com.nancho313.loqui.connector.integrationtest.contract.websocket.handler;

import com.nancho313.loqui.connector.domain.vo.UserConnectionStatus;
import com.nancho313.loqui.connector.infrastructure.client.redis.dao.UserConnectionRedisDao;
import com.nancho313.loqui.connector.integrationtest.BaseIntegrationTest;
import com.nancho313.loqui.connector.integrationtest.util.kafka.KafkaMessageCaptor;
import com.nancho313.loqui.connector.integrationtest.util.websocket.TestWebSocketClient;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

public class UserNotificationsSocketHandlerIT extends BaseIntegrationTest {

  private List<TestWebSocketClient> clients;

  @LocalServerPort
  private int port;

  @Autowired
  private WebSocketSessionHub webSocketSessionHub;

  @Autowired
  private UserConnectionRedisDao userConnectionRedisDao;

  @Autowired
  private String connectorId;

  @Autowired
  private KafkaMessageCaptor<UserNotificationEvent> messageCaptor;

  @BeforeEach
  void setup() {

    clients = new ArrayList<>();
  }

  @AfterEach
  void teardown() {

    clients.forEach(TestWebSocketClient::disconnect);
    userConnectionRedisDao.deleteAll();
    messageCaptor.cleanMessages();
  }

  @Test
  void handShakeAndConnectOk() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var username = "foo";
    var client = createWebSocketClient(id, username);
    // Act
    client.connect();

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(id).isEmpty());
    var connectedSession = webSocketSessionHub.getSessionsByIdUser(id).getFirst();
    assertThat(connectedSession).isNotNull().isNotNull();
    assertThat(connectedSession.isOpen()).isTrue();

    var storedUserConnections = userConnectionRedisDao.findByIdUser(id);
    assertThat(storedUserConnections).isNotNull().hasSize(1);
    var storedUserConnection = storedUserConnections.getFirst();
    assertThat(storedUserConnection.idUser()).isEqualTo(id);
    assertThat(storedUserConnection.username()).isEqualTo(username);
    assertThat(storedUserConnection.connectorId()).isEqualTo(connectorId);
    assertThat(storedUserConnection.id()).isEqualTo(connectedSession.getId());
    assertThat(storedUserConnection.status()).isEqualTo(UserConnectionStatus.AVAILABLE.name());
  }

  @Test
  void disconnectOk() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var username = "foo";
    var client = createWebSocketClient(id, username);
    client.connect();
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(id).isEmpty());
    var sessionId = webSocketSessionHub.getSessionsByIdUser(id).getFirst().getId();

    // Act
    client.disconnect();

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> webSocketSessionHub.getSessionsByIdUser(id).isEmpty());
    var connectedSessions = webSocketSessionHub.getSessionsByIdUser(id);
    assertThat(connectedSessions).isNotNull().isEmpty();

    var storedUserConnections = userConnectionRedisDao.findByIdUser(id);
    assertThat(storedUserConnections).isNotNull().hasSize(1);
    var storedUserConnection = storedUserConnections.getFirst();
    assertThat(storedUserConnection.idUser()).isEqualTo(id);
    assertThat(storedUserConnection.username()).isEqualTo(username);
    assertThat(storedUserConnection.connectorId()).isEqualTo(connectorId);
    assertThat(storedUserConnection.id()).isEqualTo(sessionId);
    assertThat(storedUserConnection.status()).isEqualTo(UserConnectionStatus.DISCONNECTED.name());
  }

  @Test
  void sendMessageOk() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var client1 = createWebSocketClient(userId1, "foo1");
    var client2 = createWebSocketClient(userId2, "foo2");
    client1.connect();
    client2.connect();
    var message = "This is the content of the message.";

    // Act
    client1.sentMessage(message, userId2);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> messageCaptor.hasCapturedMessages());
    var sentMessages = messageCaptor.getCapturedMessages();
    assertThat(sentMessages).isNotNull().hasSize(1);
    var sentUserNotificationEvent = sentMessages.getFirst().getPayload();
    assertThat(sentUserNotificationEvent.getConnectorId()).hasToString(connectorId);
    assertThat(sentUserNotificationEvent.getSourceUser()).hasToString(userId1);
    assertThat(sentUserNotificationEvent.getTargetUser()).hasToString(userId2);
    assertThat(sentUserNotificationEvent.getDate()).isGreaterThan(0);

    assertThat(sentUserNotificationEvent.getNotification()).isNotNull().isInstanceOf(SentTextMessage.class);
    var sentTextMessage = (SentTextMessage) sentUserNotificationEvent.getNotification();
    assertThat(sentTextMessage.getContent()).hasToString(message);
  }

  @Test
  void sendMessageToUserWithMultipleConnectionsOk() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var client1 = createWebSocketClient(userId1, "foo1");
    var firstClient2 = createWebSocketClient(userId2, "foo2");
    var secondClient2 = createWebSocketClient(userId2, "foo2");
    client1.connect();
    firstClient2.connect();
    secondClient2.connect();
    var message = "This is the content of the message.";

    // Act
    client1.sentMessage(message, userId2);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> messageCaptor.hasCapturedMessages());
    var sentMessages = messageCaptor.getCapturedMessages();
    assertThat(sentMessages).isNotNull().hasSize(1);
    var sentUserNotificationEvent = sentMessages.getFirst().getPayload();
    assertThat(sentUserNotificationEvent.getConnectorId()).hasToString(connectorId);
    assertThat(sentUserNotificationEvent.getSourceUser()).hasToString(userId1);
    assertThat(sentUserNotificationEvent.getTargetUser()).hasToString(userId2);
    assertThat(sentUserNotificationEvent.getDate()).isGreaterThan(0);

    assertThat(sentUserNotificationEvent.getNotification()).isNotNull().isInstanceOf(SentTextMessage.class);
    var sentTextMessage = (SentTextMessage) sentUserNotificationEvent.getNotification();
    assertThat(sentTextMessage.getContent()).hasToString(message);
  }

  @Test
  void sendMessageToDisconnectedUserOk() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var client1 = createWebSocketClient(userId1, "foo1");
    var client2 = createWebSocketClient(userId2, "foo2");
    client1.connect();
    client2.connect();
    var message = "This is the content of the message.";
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(userId2).isEmpty());
    client2.disconnect();
    await().atMost(5, TimeUnit.SECONDS).until(() -> webSocketSessionHub.getSessionsByIdUser(userId2).isEmpty());

    // Act
    client1.sentMessage(message, userId2);

    // Assert
    await().pollDelay(2, TimeUnit.SECONDS).untilAsserted(() -> assertThat(Boolean.TRUE).isTrue());

    var sentMessages = messageCaptor.getCapturedMessages();
    assertThat(sentMessages).isNotNull().isEmpty();
  }

  private TestWebSocketClient createWebSocketClient(String idUser, String username) {

    var client = new TestWebSocketClient(idUser, username, port);
    clients.add(client);
    return client;
  }
}
