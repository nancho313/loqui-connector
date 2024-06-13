package com.nancho313.loqui.connector.test.application.util;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserConnectionRepositorySpy implements UserConnectionRepository {

  private final List<UserConnection> data = new ArrayList<>();

  @Override
  public Optional<UserConnection> findById(UserConnectionId id) {
    return data.stream().filter(value -> value.getId().equals(id)).findFirst();
  }

  @Override
  public UserConnection save(UserConnection userConnection) {
    data.removeIf(value -> value.getId().equals(userConnection.getId()));
    data.add(userConnection);
    return userConnection;
  }

  @Override
  public List<UserConnection> findAllAvailableByIdUser(String idUser) {
    return data.stream().filter(value -> value.getIdUser().equals(idUser)).toList();
  }

  public List<UserConnection> findAll() {

    return List.copyOf(data);
  }
}
