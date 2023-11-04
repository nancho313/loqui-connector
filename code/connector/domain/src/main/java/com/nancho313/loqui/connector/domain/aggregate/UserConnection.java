package com.nancho313.loqui.connector.domain.aggregate;

import com.nancho313.loqui.connector.domain.event.DomainEvent;
import com.nancho313.loqui.connector.domain.externalservice.IdGenerator;
import com.nancho313.loqui.connector.domain.vo.CurrentDate;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.domain.vo.UserConnectionStatus;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.nancho313.loqui.commons.ObjectValidator.isEmptyString;
import static com.nancho313.loqui.commons.ObjectValidator.isNull;

@Value
@EqualsAndHashCode(callSuper = true)
public class UserConnection extends DomainAggregate {
  
  private static final String ERROR_MESSAGE = "Cannot create an UserConnection object. Errors -> %s";
  
  UserConnectionId id;
  
  String idUser;
  
  String username;
  
  String connectorId;
  
  UserConnectionStatus status;
  
  CurrentDate currentDate;
  
  public UserConnection(UserConnectionId id, String idUser, String username, String connectorId,
                        UserConnectionStatus status, CurrentDate currentDate) {
    this(Collections.emptyList(), id, idUser, username, connectorId, status, currentDate);
  }
  
  UserConnection(List<DomainEvent> currentEvents, UserConnectionId id, String idUser, String username,
                 String connectorId, UserConnectionStatus status, CurrentDate currentDate) {
    super(currentEvents);
    this.id = id;
    this.idUser = idUser;
    this.username = username;
    this.connectorId = connectorId;
    this.status = status;
    this.currentDate = currentDate;
    validate();
  }
  
  public static UserConnection create(UserConnectionId id, String idUser, String username, String connectorId) {
    
    var event = new ConnectedUserEvent(id, idUser, username, connectorId, LocalDateTime.now());
    return new UserConnection(List.of(event), id, idUser, username, connectorId,
            UserConnectionStatus.AVAILABLE, CurrentDate.now());
  }
  
  public UserConnection disconnect() {
    
    var event = new DisconnectedUserEvent(this.id, idUser, this.username, this.connectorId, LocalDateTime.now());
    return new UserConnection(List.of(event), this.id, this.idUser, this.username, this.connectorId,
            UserConnectionStatus.DISCONNECTED, this.currentDate.update());
  }
  
  public boolean isAvailable() {
    
    return UserConnectionStatus.AVAILABLE.equals(this.status);
  }
  
  private void validate() {
    
    List<String> errors = new ArrayList<>();
    
    if (isNull(id)) {
      
      errors.add("The id cannot be null.");
    }
    
    if (isEmptyString(idUser)) {
      
      errors.add("The id user cannot be empty.");
    }
    
    if (isEmptyString(username)) {
      
      errors.add("The username cannot be empty.");
    }
    
    if (isEmptyString(connectorId)) {
      
      errors.add("The connector id cannot be empty.");
    }
    
    if (isNull(status)) {
      
      errors.add("The status cannot be null.");
    }
    
    if (isNull(currentDate)) {
      
      errors.add("The current date cannot be null.");
    }
    
    if (!errors.isEmpty()) {
      
      throw new IllegalArgumentException(ERROR_MESSAGE.formatted(errors));
    }
  }
  
  public record ConnectedUserEvent(UserConnectionId id, String idUser, String username, String connectorId,
                                   LocalDateTime date) implements DomainEvent {
  }
  
  public record DisconnectedUserEvent(UserConnectionId id, String idUser, String username, String connectorId,
                                      LocalDateTime date) implements DomainEvent {
  }
}
