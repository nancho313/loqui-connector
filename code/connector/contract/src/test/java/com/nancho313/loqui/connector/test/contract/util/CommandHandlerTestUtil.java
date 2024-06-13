package com.nancho313.loqui.connector.test.contract.util;

import com.nancho313.loqui.connector.application.command.Command;
import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolver;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import jakarta.validation.Validation;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class CommandHandlerTestUtil<T extends Command> extends CommandHandler<T> {

  private T commandToProcess;

  public CommandHandlerTestUtil() {
    super(Validation.buildDefaultValidatorFactory().getValidator(), new EventResolverFactory() {
      @Override
      public <T extends DomainEvent> Optional<EventResolver<T>> getResolver(T event) {
        return Optional.empty();
      }
    });
  }

  @Override
  protected List<DomainEvent> handleCommand(T command) {
    commandToProcess = command;
    return new ArrayList<>();
  }
}
