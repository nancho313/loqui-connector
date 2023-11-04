package com.nancho313.loqui.connector.infrastructure.client.redis.dao;

import com.nancho313.loqui.connector.infrastructure.client.redis.hash.UserConnectionHash;
import org.springframework.data.repository.CrudRepository;

import java.util.List;


public interface UserConnectionRedisDao extends CrudRepository<UserConnectionHash, String> {
  
  List<UserConnectionHash> findByIdUser(String idUser);
}
