package com.nancho313.loqui.connector.infrastructure.client.kafka.emitter;

import com.nancho313.loqui.events.UserNotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
public class UserNotificationKafkaEmitter implements LoquiKafkaEmitter<UserNotificationEvent> {
  
  private static final String TOPIC_KEY = "user-notification-connector";
  private static final String BASE_TOPIC = "user-notification-connector-%s";
  private final KafkaTemplate<String, UserNotificationEvent> kafkaTemplate;
  
  public UserNotificationKafkaEmitter(ProducerFactory<String, UserNotificationEvent> producerFactory) {
    
    this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
  }
  
  public boolean sendMessage(UserNotificationEvent message, List<Header> headers) {
    
    var record = new ProducerRecord<>(buildTopicFromMessage(message), null, TOPIC_KEY, message, headers);
    
    var operation = kafkaTemplate.send(record);
    try {
      operation.get(10, TimeUnit.SECONDS);
      return Boolean.TRUE;
    } catch (InterruptedException | ExecutionException | TimeoutException e) {
      
      log.error("There was an error while sending the message.", e);
      return Boolean.FALSE;
    }
  }
  
  private String buildTopicFromMessage(UserNotificationEvent message) {
    
    return BASE_TOPIC.formatted(message.getConnectorId());
  }
}
