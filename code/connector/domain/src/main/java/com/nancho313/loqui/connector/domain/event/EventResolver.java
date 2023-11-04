package com.nancho313.loqui.connector.domain.event;

public interface EventResolver <T extends DomainEvent>{
  
  void processEvent(T t);
  
  Class<T> getType();
}
