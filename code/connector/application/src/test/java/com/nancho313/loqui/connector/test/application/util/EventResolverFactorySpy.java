package com.nancho313.loqui.connector.test.application.util;

import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolver;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class EventResolverFactorySpy implements EventResolverFactory {

  private final List<DomainEvent> processedEvents = new ArrayList<>();

  @Override
  public <T extends DomainEvent> Optional<EventResolver<T>> getResolver(T event) {
    processedEvents.add(event);
    return Optional.empty();
  }
}
