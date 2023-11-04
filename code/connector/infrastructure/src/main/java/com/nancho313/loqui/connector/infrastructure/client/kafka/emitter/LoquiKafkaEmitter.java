package com.nancho313.loqui.connector.infrastructure.client.kafka.emitter;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.header.Header;

import java.util.List;

public interface LoquiKafkaEmitter <T extends SpecificRecordBase>{
  
  boolean sendMessage(T message, List<Header> headers);
}
