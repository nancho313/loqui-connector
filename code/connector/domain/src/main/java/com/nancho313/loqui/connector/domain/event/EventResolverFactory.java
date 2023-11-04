package com.nancho313.loqui.connector.domain.event;

import java.util.Optional;

public interface EventResolverFactory {
  
  <T extends DomainEvent> Optional<EventResolver<T>> getResolver(T event);
}
