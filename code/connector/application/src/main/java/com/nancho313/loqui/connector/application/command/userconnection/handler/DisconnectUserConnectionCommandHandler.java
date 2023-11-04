package com.nancho313.loqui.connector.application.command.userconnection.handler;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.DisconnectUserConnectionCommand;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
public class DisconnectUserConnectionCommandHandler extends CommandHandler<DisconnectUserConnectionCommand> {
  
  private static final String USER_CONNECTION_NOT_FOUND = "The user connection with id %s does not exist.";
  private final UserConnectionRepository repository;
  
  public DisconnectUserConnectionCommandHandler(Validator validator, EventResolverFactory eventResolverFactory,
                                                UserConnectionRepository repository) {
    super(validator, eventResolverFactory);
    this.repository = repository;
  }
  
  protected List<DomainEvent> handleCommand(DisconnectUserConnectionCommand command) {
    
    var userConnectionToDisconnect =
            repository.findById(UserConnectionId.of(command.connection()))
                    .orElseThrow(() -> new NoSuchElementException(USER_CONNECTION_NOT_FOUND.formatted(command.connection())));
    
    var disconnectedUser = userConnectionToDisconnect.disconnect();
    repository.save(disconnectedUser);
    return disconnectedUser.getCurrentEvents();
  }
}
