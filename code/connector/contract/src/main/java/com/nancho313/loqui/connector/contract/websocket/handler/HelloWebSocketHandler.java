package com.nancho313.loqui.connector.contract.websocket.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class HelloWebSocketHandler extends TextWebSocketHandler {
  
  private static final String AUTH_USER_KEY = "authUser";
  
  private final Set<WebSocketSession> sessions;
  
  public HelloWebSocketHandler() {
    
    sessions = new HashSet<>();
  }
  
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    
    sendMessage(session, "This is the response message of %s".formatted(message));
  }
  
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    
    sessions.add(session);
    log.info("Auth Data -> {}", session.getAttributes().get(AUTH_USER_KEY));
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
  }
  
  
  private void sendMessage(WebSocketSession session, String message) {
    
    log.info(session.getId());
    TextMessage messageToSend = new TextMessage(message);
    try {
      session.sendMessage(messageToSend);
    } catch (IOException e) {
      log.error("The message could not be sent");
    }
  }
}
