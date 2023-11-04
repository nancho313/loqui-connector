package com.nancho313.loqui.connector.domain.vo;

import java.time.LocalDateTime;

public record TextMessage(String senderUser, String targetUser, String content,
                          String connector, LocalDateTime date) {
  
  public static TextMessage create(String senderUser, String targetUser, String content,
                                   String connector) {
    
    return new TextMessage(senderUser, targetUser, content, connector, LocalDateTime.now());
  }
}
