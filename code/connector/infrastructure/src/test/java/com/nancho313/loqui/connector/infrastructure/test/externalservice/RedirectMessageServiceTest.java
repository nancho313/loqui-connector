package com.nancho313.loqui.connector.infrastructure.test.externalservice;

import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.infrastructure.client.kafka.emitter.LoquiKafkaEmitter;
import com.nancho313.loqui.connector.infrastructure.externalservice.RedirectMessageServiceImpl;
import com.nancho313.loqui.connector.infrastructure.test.util.TestWebSocketSession;
import com.nancho313.loqui.connector.infrastructure.websocket.WebSocketSessionHubImpl;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class RedirectMessageServiceTest {

  private WebSocketSessionHub webSocketSessionHub;

  private LoquiKafkaEmitter<UserNotificationEvent> emitterMock;

  private RedirectMessageServiceImpl sut;

  @BeforeEach
  void setup() {

    emitterMock = mock(LoquiKafkaEmitter.class);
    webSocketSessionHub = new WebSocketSessionHubImpl();
    sut = new RedirectMessageServiceImpl(emitterMock, webSocketSessionHub);
  }

  @Test
  void redirectMessageOk() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var message = TextMessage.create(senderUser, targetUser, content, connector);
    when(emitterMock.sendMessage(any(UserNotificationEvent.class), anyList())).thenReturn(Boolean.TRUE);

    // Act
    var result = sut.redirectMessage(message);

    // Assert
    assertThat(result).isTrue();

    var argCaptor = ArgumentCaptor.forClass(UserNotificationEvent.class);
    verify(emitterMock).sendMessage(argCaptor.capture(), anyList());
    var capturedValue = argCaptor.getValue();
    assertThat(capturedValue).isNotNull();
    assertThat(capturedValue.getConnectorId()).hasToString(connector);
    assertThat(capturedValue.getSourceUser()).hasToString(senderUser);
    assertThat(capturedValue.getTargetUser()).hasToString(targetUser);
    assertThat(capturedValue.getNotification()).isNotNull().isInstanceOf(SentTextMessage.class);
    var sentTextMessage = (SentTextMessage) capturedValue.getNotification();
    assertThat(sentTextMessage.getContent()).hasToString(content);
  }

  @Test
  void redirectMessageReturnsFalseOk() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var message = TextMessage.create(senderUser, targetUser, content, connector);
    when(emitterMock.sendMessage(any(UserNotificationEvent.class), anyList())).thenReturn(Boolean.FALSE);

    // Act
    var result = sut.redirectMessage(message);

    // Assert
    assertThat(result).isFalse();
  }

  @Test
  void sendMessageOk() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var message = TextMessage.create(senderUser, targetUser, content, connector);

    var webSocketSession = TestWebSocketSession.create(targetUser, "foo");
    webSocketSessionHub.addSession(webSocketSession);

    // Act
    var result = sut.sendMessage(message);

    // Assert
    assertThat(result).isTrue();
    var sentMessages = webSocketSession.getSentMessages();
    assertThat(sentMessages).isNotNull().hasSize(1).allMatch(value -> value instanceof org.springframework.web.socket.TextMessage);
    var sentMessage = (org.springframework.web.socket.TextMessage) sentMessages.getFirst();
    assertThat(sentMessage.getPayload()).contains(senderUser);
    assertThat(sentMessage.getPayload()).contains(targetUser);
    assertThat(sentMessage.getPayload()).contains(content);
    assertThat(sentMessage.getPayload()).contains(message.date().format(DateTimeFormatter.ISO_DATE));
  }

  @Test
  void sendMessageReturnsFalse() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var message = TextMessage.create(senderUser, targetUser, content, connector);

    var webSocketSession = TestWebSocketSession.create(targetUser, "foo");
    webSocketSession.enableErrorMode();
    webSocketSessionHub.addSession(webSocketSession);

    // Act
    var result = sut.sendMessage(message);

    // Assert
    assertThat(result).isFalse();
    var sentMessages = webSocketSession.getSentMessages();
    assertThat(sentMessages).isNotNull().hasSize(1).allMatch(value -> value instanceof org.springframework.web.socket.TextMessage);
    var sentMessage = (org.springframework.web.socket.TextMessage) sentMessages.getFirst();
    assertThat(sentMessage.getPayload()).contains(senderUser);
    assertThat(sentMessage.getPayload()).contains(targetUser);
    assertThat(sentMessage.getPayload()).contains(content);
    assertThat(sentMessage.getPayload()).contains(message.date().format(DateTimeFormatter.ISO_DATE));
  }

  @Test
  void sendMessageThrowsExceptionDueSessionWasNotFound() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var message = TextMessage.create(senderUser, targetUser, content, connector);

    // Act & Assert
    var exception = assertThrows(NoSuchElementException.class, () -> sut.sendMessage(message));
    assertThat(exception.getMessage()).contains("No session was found for the user id " + targetUser + ".");
  }
}
