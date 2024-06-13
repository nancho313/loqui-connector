package com.nancho313.loqui.connector.integrationtest.util.kafka;

import com.nancho313.loqui.events.UserNotificationEvent;
import org.springframework.stereotype.Component;

@Component
@ITKafkaListener(topics = "#{__listener.topic}")
public class UserNotificationEventMessageCaptor extends KafkaMessageCaptor<UserNotificationEvent> {

  private static final String USER_NOTIFICATION_CONNECTOR = "user-notification-connector-";

  private final String topic;

  public UserNotificationEventMessageCaptor(String connectorId) {
    this.topic = USER_NOTIFICATION_CONNECTOR+connectorId;
  }

  public String getTopic() {
    return topic;
  }
}