package com.nancho313.loqui.connector.infrastructure.client.redis.hash;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;

@RedisHash(value = "userConnection", timeToLive = 86400)
public record UserConnectionHash(@Id String id, @Indexed String idUser, String username,
                                 String connectorId, String status, LocalDateTime creationDate,
                                 LocalDateTime lastUpdatedDate) {
}
