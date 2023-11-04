package com.nancho313.loqui.connector.domain.repository;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;

import java.util.List;
import java.util.Optional;

public interface UserConnectionRepository {
  
  Optional<UserConnection> findById(UserConnectionId id);
  
  UserConnection save(UserConnection userConnection);
  
  List<UserConnection> findAllAvailableByIdUser(String idUser);
}
