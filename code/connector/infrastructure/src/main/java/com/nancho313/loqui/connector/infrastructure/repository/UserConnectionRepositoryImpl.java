package com.nancho313.loqui.connector.infrastructure.repository;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.repository.UserConnectionRepository;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.infrastructure.client.redis.dao.UserConnectionRedisDao;
import com.nancho313.loqui.connector.infrastructure.mapper.UserConnectionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserConnectionRepositoryImpl implements UserConnectionRepository {
  
  private final UserConnectionRedisDao dao;
  
  private final UserConnectionMapper mapper;
  
  public Optional<UserConnection> findById(UserConnectionId id) {
    return dao.findById(id.id()).map(mapper::toEntity);
  }
  
  public UserConnection save(UserConnection userConnection) {
    return mapper.toEntity(dao.save(mapper.toHash(userConnection)));
  }
  
  public List<UserConnection> findAllAvailableByIdUser(String idUser) {
    return dao.findByIdUser(idUser).stream().map(mapper::toEntity).filter(UserConnection::isAvailable).toList();
  }
}
