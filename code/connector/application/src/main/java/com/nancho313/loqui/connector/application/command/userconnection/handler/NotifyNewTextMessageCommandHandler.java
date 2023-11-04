package com.nancho313.loqui.connector.application.command.userconnection.handler;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.NotifyNewTextMessageCommand;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import com.nancho313.loqui.connector.domain.externalservice.IdGenerator;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotifyNewTextMessageCommandHandler extends CommandHandler<NotifyNewTextMessageCommand> {
  
  private final IdGenerator idGenerator;
  
  private final UserConnectionRepository repository;
  
  public NotifyNewTextMessageCommandHandler(Validator validator, EventResolverFactory eventResolverFactory,
                                            IdGenerator idGenerator, UserConnectionRepository repository) {
    super(validator, eventResolverFactory);
    this.idGenerator = idGenerator;
    this.repository = repository;
  }
  
  protected List<DomainEvent> handleCommand(NotifyNewTextMessageCommand command) {
    
    return null;
  }
}
