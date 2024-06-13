package com.nancho313.loqui.connector.domain.domainservice;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public final class MessageResolver {
  
  private final RedirectMessageService redirectMessage;
  
  public List<RedirectedTextMessageEvent> redirectMessage(UserConnection senderUser, List<UserConnection> targetUsers,
                                                          String content, LocalDateTime date) {

    assert(senderUser != null);
    assert(targetUsers != null);
    assert(content != null && !content.isBlank());

    List<RedirectedTextMessageEvent> result = new ArrayList<>();
    
    targetUsers.stream().filter(UserConnection::isAvailable).forEach(userConnection -> {
      
      var textMessage = TextMessage.create(senderUser.getIdUser(), userConnection.getIdUser(), content,
              userConnection.getConnectorId(), date);
      var wasSent = redirectMessage.redirectMessage(textMessage);
      if (wasSent) {
        
        result.add(new RedirectedTextMessageEvent(textMessage));
      }
    });
    
    return result;
  }
  
  public record RedirectedTextMessageEvent(TextMessage message) implements DomainEvent {
  }
}
