package com.nancho313.loqui.connector.domain.externalservice;

import com.nancho313.loqui.connector.domain.vo.TextMessage;

public interface RedirectMessageService {
  
  boolean redirectMessage(TextMessage message);
  
  boolean sendMessage(TextMessage message);
}
