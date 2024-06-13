package com.nancho313.loqui.connector.test.contract.kafka.listener;

import com.nancho313.loqui.connector.application.command.userconnection.command.SendTextMessageCommand;
import com.nancho313.loqui.connector.contract.kafka.listener.UserNotificationKafkaListener;
import com.nancho313.loqui.connector.test.contract.util.CommandHandlerTestUtil;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentNotification;
import com.nancho313.loqui.events.usernotification.SentNotificationType;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.messaging.support.GenericMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserNotificationKafkaListenerTest {

  private final String connectorId = UUID.randomUUID().toString();

  private CommandHandlerTestUtil<SendTextMessageCommand> commandHandler;

  private UserNotificationKafkaListener sut;

  @BeforeEach
  void setup() {

    commandHandler = new CommandHandlerTestUtil<>();
    sut = new UserNotificationKafkaListener(connectorId, commandHandler);
  }

  @Test
  void consumeMessageOk() {

    // Arrange
    var targetUser = UUID.randomUUID().toString();
    var sourceUser = UUID.randomUUID().toString();
    var date = Instant.now().toEpochMilli();
    var notification = new SentTextMessage("This is the message to send.");
    var payload = new UserNotificationEvent(targetUser, sourceUser, connectorId, date, notification);
    var message = new GenericMessage<>(payload);

    // Act
    sut.consumeMessage(message);

    // Assert
    var commandToProcess = commandHandler.getCommandToProcess();
    assertThat(commandToProcess).isNotNull();
    assertThat(commandToProcess.userId()).isEqualTo(sourceUser);
    assertThat(commandToProcess.targetUser()).isEqualTo(targetUser);
    assertThat(commandToProcess.connector()).isEqualTo(connectorId);
    assertThat(commandToProcess.date()).isCloseTo(Instant.ofEpochMilli(date).atZone(ZoneId.of("UTC")).toLocalDateTime(), within(100, ChronoUnit.MILLIS));
    assertThat(commandToProcess.content()).isEqualTo(notification.getContent().toString());
  }

  @Test
  void consumeMessageUserNotificationsIsNotSupported() {

    // Arrange
    var targetUser = UUID.randomUUID().toString();
    var sourceUser = UUID.randomUUID().toString();
    var date = Instant.now().toEpochMilli();
    var notification = new SentNotification("description notification", SentNotificationType.INFO);
    var payload = new UserNotificationEvent(targetUser, sourceUser, connectorId, date, notification);
    var message = new GenericMessage<>(payload);

    // Act & Assert
    var exception = assertThrows(UnsupportedOperationException.class, () -> sut.consumeMessage(message));
    assertThat(exception.getMessage()).contains("Handling notifications is not supported, yet.");
  }

  @Test
  void getTopicOk() {

    // Act
    var result = sut.getTopic();

    // Assert
    assertThat(result).isEqualTo("user-notification-connector-" + connectorId);
  }
}
