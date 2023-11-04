package com.nancho313.loqui.connector.application.command.userconnection.handler;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.RedirectMessageCommand;
import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.domainservice.MessageResolver;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class RedirectMessageCommandHandler extends CommandHandler<RedirectMessageCommand> {
  
  private static final String USER_CONNECTION_NOT_FOUND = "The user connection with id %s does not exist.";
  
  private final UserConnectionRepository userConnectionRepository;
  
  private final MessageResolver messageResolver;
  
  public RedirectMessageCommandHandler(Validator validator, EventResolverFactory eventResolverFactory,
                                       UserConnectionRepository userConnectionRepository,
                                       RedirectMessageService redirectMessage) {
    super(validator, eventResolverFactory);
    this.userConnectionRepository = userConnectionRepository;
    this.messageResolver = new MessageResolver(redirectMessage);
  }
  
  protected List<DomainEvent> handleCommand(RedirectMessageCommand command) {
    
    var senderUser =
            userConnectionRepository.findById(UserConnectionId.of(command.connection()))
                    .orElseThrow(() -> new NoSuchElementException(USER_CONNECTION_NOT_FOUND.formatted(command.connection())));
    
    List<UserConnection> availableConnections =
            userConnectionRepository.findAllAvailableByIdUser(command.targetIdUser());
    
    return new ArrayList<>(messageResolver.redirectMessage(senderUser, availableConnections, command.content()));
  }
}
