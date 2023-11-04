package com.nancho313.loqui.connector.infrastructure.event;

import com.nancho313.loqui.connector.domain.domainservice.MessageResolver;
import com.nancho313.loqui.connector.domain.event.EventResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SentTextMessageEventResolver implements EventResolver<MessageResolver.RedirectedTextMessageEvent> {
  
  
  
  public void processEvent(MessageResolver.RedirectedTextMessageEvent sentTextMessageEvent) {
  
  
  }
  
  public Class<MessageResolver.RedirectedTextMessageEvent> getType() {
    return MessageResolver.RedirectedTextMessageEvent.class;
  }
}
