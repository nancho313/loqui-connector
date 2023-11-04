package com.nancho313.loqui.connector.domain.vo;

public record UserConnectionId(String id) {

  public static UserConnectionId of(String id) {
    
    return new UserConnectionId(id);
  }
}
