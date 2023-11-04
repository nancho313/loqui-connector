package com.nancho313.loqui.connector.contract.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class KafkaListenerConfig {
  
  private final String connectorId;
  
  public KafkaListenerConfig() {
    this.connectorId = UUID.randomUUID().toString();
  }
  
  @Bean
  public String connectorId() {
    
    return this.connectorId;
  }
}
