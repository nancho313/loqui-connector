package com.nancho313.loqui.connector.contract.kafka.config;

import com.nancho313.loqui.commons.AvroSerializer;
import com.nancho313.loqui.events.AcceptedContactRequestEvent;
import com.nancho313.loqui.events.CreatedUserEvent;
import com.nancho313.loqui.events.UserNotificationEvent;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serializer;

import java.util.HashMap;
import java.util.Map;

public class LoquiMessageSerializer<T extends SpecificRecordBase> implements Deserializer<T>, Serializer<T> {
  
  private static final String USER_NOTIFICATION_CONNECTOR_KEY = "user-notification-connector";
  private final Map<String, AvroSerializer<T>> avroSerializers;
  
  public LoquiMessageSerializer() {
    
    avroSerializers = new HashMap<>();
    avroSerializers.put("accepted-contact-request", new AvroSerializer<>(AcceptedContactRequestEvent.getClassSchema()));
    avroSerializers.put("created-users", new AvroSerializer<>(CreatedUserEvent.getClassSchema()));
    avroSerializers.put(USER_NOTIFICATION_CONNECTOR_KEY, new AvroSerializer<>(UserNotificationEvent.getClassSchema()));
  }
  
  public T deserialize(String key, byte[] bytes) {
    
    if (key.contains(USER_NOTIFICATION_CONNECTOR_KEY)) {
      return avroSerializers.get(USER_NOTIFICATION_CONNECTOR_KEY).deserialize(bytes);
    }
    return avroSerializers.get(key).deserialize(bytes);
  }
  
  public byte[] serialize(String key, T message) {
    
    if (key.contains(USER_NOTIFICATION_CONNECTOR_KEY)) {
      return avroSerializers.get(USER_NOTIFICATION_CONNECTOR_KEY).serialize(message);
    }
    return avroSerializers.get(key).serialize(message);
  }
  
  public void configure(Map<String, ?> configs, boolean isKey) {
    Deserializer.super.configure(configs, isKey);
  }
  
  public T deserialize(String topic, Headers headers, byte[] data) {
    return Deserializer.super.deserialize(topic, headers, data);
  }
  
  public void close() {
    Deserializer.super.close();
  }
  
  public byte[] serialize(String topic, Headers headers, T data) {
    return Serializer.super.serialize(topic, headers, data);
  }
}
