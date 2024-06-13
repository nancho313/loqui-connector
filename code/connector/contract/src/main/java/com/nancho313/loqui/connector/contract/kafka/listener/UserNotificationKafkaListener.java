package com.nancho313.loqui.connector.contract.kafka.listener;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.SendTextMessageCommand;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Slf4j
@Component
public class UserNotificationKafkaListener {
  
  private static final String USER_NOTIFICATION_CONNECTOR = "user-notification-connector-";
  
  private final String topic;
  
  private final CommandHandler<SendTextMessageCommand> sendTextMessageCommandHandler;
  
  public UserNotificationKafkaListener(String connectorId,
                                       CommandHandler<SendTextMessageCommand> sendTextMessageCommandHandler) {
    this.topic = USER_NOTIFICATION_CONNECTOR + connectorId;
    this.sendTextMessageCommandHandler = sendTextMessageCommandHandler;
  }
  
  @KafkaListener(topics = "#{__listener.topic}", groupId = "loqui-connector")
  public void consumeMessage(Message<UserNotificationEvent> message) {
    
    var value = message.getPayload();
    var notification = value.getNotification();
    
    if (notification instanceof SentTextMessage sentMessage) {
      
      var dateFromMilliseconds = LocalDateTime.ofInstant(Instant.ofEpochMilli(value.getDate()), ZoneOffset.UTC);
      var command = new SendTextMessageCommand(value.getSourceUser().toString(), value.getTargetUser().toString(),
              sentMessage.getContent().toString(), value.getConnectorId().toString(), dateFromMilliseconds);
      sendTextMessageCommandHandler.handle(command);
    } else {
      throw new UnsupportedOperationException("Handling notifications is not supported, yet.");
    }
  }
  
  public String getTopic() {
    return topic;
  }
}
