package com.nancho313.loqui.connector.domain.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.nancho313.loqui.commons.ObjectValidator.*;

public record TextMessage(String senderUser, String targetUser, String content,
                          String connector, LocalDateTime date) {

  public TextMessage {

    List<String> errors = new ArrayList<>();

    if (isEmptyString(senderUser)) {

      errors.add("The sender user cannot be empty.");
    }

    if (isEmptyString(targetUser)) {

      errors.add("The target user cannot be empty.");
    }

    if(isEmptyString(content)) {

      errors.add("The content cannot be empty.");
    }

    if(isEmptyString(connector)) {

      errors.add("The connector cannot be empty.");
    }

    if(isNull(date)) {

      errors.add("The date cannot be null.");
    }

    if (!errors.isEmpty()) {

      throw new IllegalArgumentException("Cannot create a TextMessage object. Errors -> %s".formatted(errors));
    }
  }
  
  public static TextMessage create(String senderUser, String targetUser, String content,
                                   String connector) {
    
    return new TextMessage(senderUser, targetUser, content, connector, LocalDateTime.now());
  }
}
