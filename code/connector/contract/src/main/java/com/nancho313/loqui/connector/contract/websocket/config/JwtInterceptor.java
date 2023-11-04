package com.nancho313.loqui.connector.contract.websocket.config;

import com.nancho313.loqui.connector.websocket.dto.AuthUser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import javax.crypto.SecretKey;
import java.util.Map;

@Slf4j
public class JwtInterceptor implements HandshakeInterceptor {
  
  private static final String SAFE_PROTOCOL = "LOQUI_SAFE";
  private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";
  private static final String AUTH_USER_KEY = "authUser";
  private final SecretKey secretKey;
  
  public JwtInterceptor(String jwtKey) {
    this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtKey));
  }
  
  public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                 Map<String, Object> attributes) {
    
    try {
      
      var token = getTokenFromRequest(request);
      var claims = Jwts.parser().verifyWith(secretKey).build().parseSignedClaims(token);
      var authUser = new AuthUser(claims.getPayload().getSubject(), claims.getPayload().get("lqu", String.class));
      attributes.put(AUTH_USER_KEY, authUser);
      return true;
      
    } catch (Exception e) {
      
      return false;
    }
  }
  
  public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                             Exception exception) {
    
    response.getHeaders().add(SEC_WEB_SOCKET_PROTOCOL, SAFE_PROTOCOL);
  }
  
  private String getTokenFromRequest(ServerHttpRequest request) {
    
    return request.getHeaders().get(SEC_WEB_SOCKET_PROTOCOL).get(0).split(",")[0];
  }
}
