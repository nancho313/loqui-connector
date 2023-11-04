package com.nancho313.loqui.connector.infrastructure.mapper;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.infrastructure.client.redis.hash.UserConnectionHash;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserConnectionMapper {
  
  @Mapping(source = "id", target = "id.id")
  @Mapping(source = "creationDate", target = "currentDate.creationDate")
  @Mapping(source = "lastUpdatedDate", target = "currentDate.lastUpdatedDate")
  UserConnection toEntity(UserConnectionHash hash);
  
  @Mapping(target = "id", source = "id.id")
  @Mapping(target = "creationDate", source = "currentDate.creationDate")
  @Mapping(target = "lastUpdatedDate", source = "currentDate.lastUpdatedDate")
  UserConnectionHash toHash(UserConnection entity);
}
