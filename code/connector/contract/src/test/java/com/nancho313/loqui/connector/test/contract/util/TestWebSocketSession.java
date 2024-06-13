package com.nancho313.loqui.connector.test.contract.util;

import com.nancho313.loqui.connector.websocket.dto.AuthUser;
import org.springframework.http.HttpHeaders;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketExtension;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TestWebSocketSession implements WebSocketSession {

  public static final String AUTH_USER_KEY = "authUser";

  private String id;

  private Map<String, Object> attributes;

  private List<WebSocketMessage<?>> sentMessages;

  private boolean errorMode = false;

  private TestWebSocketSession(Map<String, Object> attributes) {
    this.id = UUID.randomUUID().toString();
    this.attributes = attributes;
    this.sentMessages = new ArrayList<>();
  }

  public static TestWebSocketSession create(String id, String username) {

    Map<String, Object> attributes = Map.of(AUTH_USER_KEY, new AuthUser(id, username));
    return new TestWebSocketSession(attributes);
  }

  @Override
  public String getId() {
    return this.id;
  }

  @Override
  public URI getUri() {
    return null;
  }

  @Override
  public HttpHeaders getHandshakeHeaders() {
    return null;
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Principal getPrincipal() {
    return null;
  }

  @Override
  public InetSocketAddress getLocalAddress() {
    return null;
  }

  @Override
  public InetSocketAddress getRemoteAddress() {
    return null;
  }

  @Override
  public String getAcceptedProtocol() {
    return "";
  }

  @Override
  public void setTextMessageSizeLimit(int messageSizeLimit) {

  }

  @Override
  public int getTextMessageSizeLimit() {
    return 0;
  }

  @Override
  public void setBinaryMessageSizeLimit(int messageSizeLimit) {

  }

  @Override
  public int getBinaryMessageSizeLimit() {
    return 0;
  }

  @Override
  public List<WebSocketExtension> getExtensions() {
    return List.of();
  }

  @Override
  public void sendMessage(WebSocketMessage<?> message) throws IOException {

    sentMessages.add(message);
    if (errorMode) {
      throw new IOException("Test error message");
    }
  }

  @Override
  public boolean isOpen() {
    return false;
  }

  @Override
  public void close() throws IOException {

  }

  @Override
  public void close(CloseStatus status) throws IOException {

  }

  public void enableErrorMode() {
    this.errorMode = true;
  }

  public List<WebSocketMessage<?>> getSentMessages() {
    return sentMessages;
  }
}
