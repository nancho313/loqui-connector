package com.nancho313.loqui.connector.infrastructure.client.redis.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;

@Configuration
@EnableRedisRepositories
public class RedisConfiguration {
  
  @Bean
  public RedisConnectionFactory connectionFactory() {
    //TODO configure connection to another environment
    RedisConnectionFactory connectionFactory = new LettuceConnectionFactory();
    return new LettuceConnectionFactory();
  }
  
  @Bean
  public RedisTemplate<?, ?> redisTemplate(RedisConnectionFactory connectionFactory) {
    
    RedisTemplate<byte[], byte[]> template = new RedisTemplate<>();
    template.setConnectionFactory(connectionFactory);
    return template;
  }
}
