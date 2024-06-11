package com.nancho313.loqui.connector.websocket.service;

import org.springframework.web.socket.WebSocketSession;

import java.util.List;
import java.util.Optional;

public interface WebSocketSessionHub {
  
  List<WebSocketSession> getSessionsByIdUser(String idUser);
  
  Optional<WebSocketSession> getSessionById(String id);
  
  void removeSession(WebSocketSession session);
  
  void addSession(WebSocketSession session);
}
