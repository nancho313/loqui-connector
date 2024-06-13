package com.nancho313.loqui.connector.integrationtest.contract.kafka.listener;

import com.nancho313.loqui.connector.contract.kafka.listener.UserNotificationKafkaListener;
import com.nancho313.loqui.connector.infrastructure.client.redis.dao.UserConnectionRedisDao;
import com.nancho313.loqui.connector.integrationtest.BaseIntegrationTest;
import com.nancho313.loqui.connector.integrationtest.util.websocket.TestWebSocketClient;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

class UserNotificationKafkaListenerIT extends BaseIntegrationTest {

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
  private UserNotificationKafkaListener sut;

  @BeforeEach
  void setup() {

    clients = new ArrayList<>();
  }

  @AfterEach
  void teardown() {

    clients.forEach(TestWebSocketClient::disconnect);
    userConnectionRedisDao.deleteAll();
  }

  @Test
  void consumeOk() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var date = Instant.now().toEpochMilli();
    var content = "This is the content of the message";
    var notification = new SentTextMessage(content);
    var client1 = createWebSocketClient(userId1, "foo1");
    var client2 = createWebSocketClient(userId2, "foo2");
    client1.connect();
    client2.connect();
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(userId1).isEmpty()
            && !webSocketSessionHub.getSessionsByIdUser(userId2).isEmpty());
    var payload = new UserNotificationEvent(userId2, userId1, connectorId, date, notification);
    Message<UserNotificationEvent> message = new GenericMessage<>(payload);

    // Act
    sut.consumeMessage(message);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> !client2.getReceivedMessages().isEmpty());
    var receivedMessages = client2.getReceivedMessages();
    assertThat(receivedMessages).isNotNull().hasSize(1);
    var receivedMessage = receivedMessages.getFirst();
    assertThat(receivedMessage.content()).isEqualTo(content);
    assertThat(receivedMessage.sender()).isEqualTo(userId1);
    assertThat(receivedMessage.target()).isEqualTo(userId2);
    assertThat(receivedMessage.date()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

  }

  @Test
  void consumeWhenTargetUserHasMultipleConnectionsOk() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var date = Instant.now().toEpochMilli();
    var content = "This is the content of the message";
    var notification = new SentTextMessage(content);
    var client1 = createWebSocketClient(userId1, "foo1");
    var firstClient2 = createWebSocketClient(userId2, "foo2");
    var secondClient2 = createWebSocketClient(userId2, "foo2");
    client1.connect();
    firstClient2.connect();
    secondClient2.connect();
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(userId1).isEmpty()
            && !webSocketSessionHub.getSessionsByIdUser(userId2).isEmpty());

    var payload = new UserNotificationEvent(userId2, userId1, connectorId, date, notification);
    Message<UserNotificationEvent> message = new GenericMessage<>(payload);

    // Act
    sut.consumeMessage(message);

    // Assert
    await().atMost(5, TimeUnit.SECONDS).until(() -> !firstClient2.getReceivedMessages().isEmpty());
    var receivedMessages = firstClient2.getReceivedMessages();
    assertThat(receivedMessages).isNotNull().hasSize(1);
    var receivedMessage = receivedMessages.getFirst();
    assertThat(receivedMessage.content()).isEqualTo(content);
    assertThat(receivedMessage.sender()).isEqualTo(userId1);
    assertThat(receivedMessage.target()).isEqualTo(userId2);
    assertThat(receivedMessage.date()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));

    await().atMost(5, TimeUnit.SECONDS).until(() -> !secondClient2.getReceivedMessages().isEmpty());
    receivedMessages = secondClient2.getReceivedMessages();
    assertThat(receivedMessages).isNotNull().hasSize(1);
    receivedMessage = receivedMessages.getFirst();
    assertThat(receivedMessage.content()).isEqualTo(content);
    assertThat(receivedMessage.sender()).isEqualTo(userId1);
    assertThat(receivedMessage.target()).isEqualTo(userId2);
    assertThat(receivedMessage.date()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
  }

  @Test
  void consumeWhenTargetUserIsDisconnected() {

    // Arrange
    var userId1 = UUID.randomUUID().toString();
    var userId2 = UUID.randomUUID().toString();
    var date = Instant.now().toEpochMilli();
    var content = "This is the content of the message";
    var notification = new SentTextMessage(content);
    var client1 = createWebSocketClient(userId1, "foo1");
    client1.connect();
    await().atMost(5, TimeUnit.SECONDS).until(() -> !webSocketSessionHub.getSessionsByIdUser(userId1).isEmpty());

    var payload = new UserNotificationEvent(userId2, userId1, connectorId, date, notification);
    Message<UserNotificationEvent> message = new GenericMessage<>(payload);

    // Act & Assert
    var exception = assertThrows(NoSuchElementException.class, () -> sut.consumeMessage(message));
    assertThat(exception.getMessage()).contains("No session was found for the user id "+userId2);

  }

  private TestWebSocketClient createWebSocketClient(String idUser, String username) {

    var client = new TestWebSocketClient(idUser, username, port);
    clients.add(client);
    return client;
  }
}
