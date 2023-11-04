package com.nancho313.loqui.connector.application.command.userconnection.handler;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.AddNewUserConnectionCommand;
import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddNewUserConnectionCommandHandler extends CommandHandler<AddNewUserConnectionCommand> {
  
  private final UserConnectionRepository repository;
  
  private final String connectorId;
  
  public AddNewUserConnectionCommandHandler(Validator validator, EventResolverFactory eventResolverFactory,
                                            UserConnectionRepository repository, String connectorId) {
    super(validator, eventResolverFactory);
    this.repository = repository;
    this.connectorId = connectorId;
  }
  
  protected List<DomainEvent> handleCommand(AddNewUserConnectionCommand command) {
    
    var newUserConnection = UserConnection.create(UserConnectionId.of(command.connectionId()), command.idUser(),
            command.username(), connectorId);
    repository.save(newUserConnection);
    return newUserConnection.getCurrentEvents();
  }
}
