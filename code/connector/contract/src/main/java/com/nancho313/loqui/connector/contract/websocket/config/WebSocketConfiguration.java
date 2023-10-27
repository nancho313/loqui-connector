package com.nancho313.loqui.connector.contract.websocket.config;

import com.nancho313.loqui.connector.contract.websocket.handler.HelloWebSocketHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
public class WebSocketConfiguration implements WebSocketConfigurer {
  
  private final HelloWebSocketHandler handler;
  
  private final String jwtKey;
  
  public WebSocketConfiguration(HelloWebSocketHandler handler, @Value("${loqui.auth.jwt.key}") String jwtKey) {
    this.handler = handler;
    this.jwtKey = jwtKey;
  }
  
  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    
    registry.addHandler(handler, "/hello").setAllowedOrigins("*").addInterceptors(new JwtInterceptor(jwtKey));
  }
}
