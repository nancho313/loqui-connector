package com.nancho313.loqui.connector.infrastructure.externalservice;

import com.nancho313.loqui.connector.domain.externalservice.IdGenerator;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdGeneratorImpl implements IdGenerator {
  
  public String generateId() {
    return UUID.randomUUID().toString();
  }
}
