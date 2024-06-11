package com.nancho313.loqui.connector.domain.vo;

import java.util.ArrayList;
import java.util.List;

import static com.nancho313.loqui.commons.ObjectValidator.isEmptyString;

public record UserConnectionId(String id) {

  public UserConnectionId {

    List<String> errors = new ArrayList<>();

    if (isEmptyString(id)) {

      errors.add("The id cannot be empty.");
    }

    if (!errors.isEmpty()) {

      throw new IllegalArgumentException("Cannot create an UserConnectionId object. Errors -> %s".formatted(errors));
    }
  }

  public static UserConnectionId of(String id) {

    return new UserConnectionId(id);
  }
}
