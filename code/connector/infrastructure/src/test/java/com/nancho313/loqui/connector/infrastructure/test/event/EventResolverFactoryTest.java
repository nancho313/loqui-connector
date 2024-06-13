package com.nancho313.loqui.connector.infrastructure.test.event;

import com.nancho313.loqui.connector.domain.domainservice.MessageResolver;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolver;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.infrastructure.event.EventResolverFactoryImpl;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EventResolverFactoryTest {

  private List<EventResolver<?>> currentEventResolvers;

  private EventResolverFactoryImpl sut;

  @BeforeEach
  void setup() {

    currentEventResolvers = new ArrayList<>();
    sut = new EventResolverFactoryImpl(currentEventResolvers);
  }

  @Test
  void getResolverOk() {

    // Arrange
    var eventResolver = new EventResolverUtilTest();
    currentEventResolvers.add(eventResolver);
    var event = new DomainEventUtilTest(UUID.randomUUID().toString());

    // Act
    var result = sut.getResolver(event);

    // Assert
    assertThat(result).isNotNull().isPresent();
    assertThat(result.get().getType()).isEqualTo(eventResolver.getType());
  }

  @Test
  void getResolverReturnsEmpty() {

    // Arrange
    var event = new DomainEventUtilTest(UUID.randomUUID().toString());

    // Act
    var result = sut.getResolver(event);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void getResolverReturnsEmptyWithNonEmptyResolvers() {

    // Arrange
    currentEventResolvers.add(new EventResolverUtilTest());

    var message = TextMessage.create(UUID.randomUUID().toString(), UUID.randomUUID().toString(),
        "This is the content", UUID.randomUUID().toString(), LocalDateTime.now());

    var event = new MessageResolver.RedirectedTextMessageEvent(message);

    // Act
    var result = sut.getResolver(event);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Getter
  private class DomainEventUtilTest implements DomainEvent {

    private String id;

    public DomainEventUtilTest(String id) {
      this.id = id;
    }
  }

  @Getter
  private class EventResolverUtilTest implements EventResolver<DomainEventUtilTest> {

    private List<DomainEventUtilTest> processedEvents = new ArrayList<>();

    @Override
    public void processEvent(DomainEventUtilTest domainEventUtilTest) {

      processedEvents.add(domainEventUtilTest);
    }

    @Override
    public Class<DomainEventUtilTest> getType() {
      return DomainEventUtilTest.class;
    }
  }

}
