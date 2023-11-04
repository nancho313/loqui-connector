package com.nancho313.loqui.connector.application.command.userconnection.handler;

import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.SendTextMessageCommand;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.event.EventResolverFactory;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SendTextMessageCommandHandler extends CommandHandler<SendTextMessageCommand> {
  
  private final RedirectMessageService redirectMessageService;
  
  public SendTextMessageCommandHandler(Validator validator, EventResolverFactory eventResolverFactory,
                                       RedirectMessageService redirectMessageService) {
    super(validator, eventResolverFactory);
    this.redirectMessageService = redirectMessageService;
  }
  
  protected List<DomainEvent> handleCommand(SendTextMessageCommand command) {
    var textMessage = new TextMessage(command.userId(), command.targetUser(), command.content(), command.connector(),
            command.date());
    redirectMessageService.sendMessage(textMessage);
    return Collections.emptyList();
  }
}
