package com.nancho313.loqui.connector.infrastructure.client.kafka.emitter;

import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public abstract class BaseLoquiKafkaEmitter<T extends SpecificRecordBase> implements LoquiKafkaEmitter<T> {
  
  private final KafkaTemplate<String, T> kafkaTemplate;
  
  public BaseLoquiKafkaEmitter(ProducerFactory<String, T> producerFactory) {
    
    this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
  }
  
  public boolean sendMessage(T message, List<Header> headers) {
    
    var record = new ProducerRecord<>(getTopic(), null, getTopic(), message, headers);
    
    var operation = kafkaTemplate.send(record);
    try {
      operation.get(10, TimeUnit.SECONDS);
      return Boolean.TRUE;
    } catch (ExecutionException | TimeoutException e) {
      
      log.error("There was an error while sending the message.", e);
      return Boolean.FALSE;
    } catch (InterruptedException e) {

      log.error("There was an error while sending the message.", e);
      Thread.currentThread().interrupt();
      return Boolean.FALSE;
    }
  }
  
  protected abstract String getTopic();
}
