package com.nancho313.loqui.connector.application.command;

import com.nancho313.loqui.connector.application.exception.InvalidCommandDataException;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

import java.util.List;
import java.util.Set;

public abstract class CommandHandler<T extends Command> {

  private final Validator validator;

  private final EventResolverFactory eventResolverFactory;

  public CommandHandler(Validator validator, EventResolverFactory eventResolverFactory) {
    this.validator = validator;
    this.eventResolverFactory = eventResolverFactory;
  }

  public void handle(T command) {

    validateCommand(command);
    var result = handleCommand(command);
    processEvents(result);
  }

  protected abstract List<DomainEvent> handleCommand(T command);

  private void processEvents(List<DomainEvent> events) {

    events.forEach(event -> eventResolverFactory.getResolver(event).ifPresent((resolver) -> resolver.processEvent(event)));
  }

  private void validateCommand(T data) {

    if (data == null) {

      throw new IllegalArgumentException("The command to process cannot be null.");
    }

    var errors = validateData(data);
    if (!errors.isEmpty()) {

      throw new InvalidCommandDataException(errors);
    }
  }

  private <Y> List<String> validateData(Y data) {

    Set<ConstraintViolation<Y>> violations = validator.validate(data);
    return violations.stream().map(ConstraintViolation::getMessage).toList();
  }
}
