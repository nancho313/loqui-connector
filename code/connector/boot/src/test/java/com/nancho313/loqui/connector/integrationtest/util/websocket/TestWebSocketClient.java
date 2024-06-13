package com.nancho313.loqui.connector.integrationtest.util.websocket;

import com.google.gson.*;
import com.nancho313.loqui.connector.infrastructure.externalservice.RedirectMessageServiceImpl;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.web.socket.*;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

import java.lang.reflect.Type;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class TestWebSocketClient {

  private static final Gson GSON_SERIALIZER = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->
                  ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime()).create();

  private static final Gson GSON_DESERIALIZER = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
          new LocalDateTimeTypeAdapter()).create();

  private static final String SEC_WEB_SOCKET_PROTOCOL = "Sec-WebSocket-Protocol";

  private static final String JWT_KEY = "a2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX2tleV9mb3JfdGVzdGluZ19wdXJwb3Nlc19rZXlfZm9yX3Rlc3RpbmdfcHVycG9zZXNfa2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX2tleV9mb3JfdGVzdGluZ19wdXJwb3Nlc19rZXlfZm9yX3Rlc3RpbmdfcHVycG9zZXNfa2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX2tleV9mb3JfdGVzdGluZ19wdXJwb3Nlc19rZXlfZm9yX3Rlc3RpbmdfcHVycG9zZXNfa2V5X2Zvcl90ZXN0aW5nX3B1cnBvc2VzX2tleV9mb3JfdGVzdGluZ19wdXJwb3Nlc19rZXlfZm9yX3Rlc3RpbmdfcHVycG9zZXM=";

  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

  private final WebSocketClient webSocketClient;

  private WebSocketSession currentSession;

  private final String id;

  private final String username;

  private final int port;

  private final TestWebSocketHandler webSocketHandler;

  public TestWebSocketClient(String id, String username, int port) {
    this.webSocketClient = new StandardWebSocketClient();
    this.id = id;
    this.username = username;
    this.port = port;
    webSocketHandler = new TestWebSocketHandler();
  }

  public void connect() {

    WebSocketHttpHeaders webSocketHttpHeaders = new WebSocketHttpHeaders();
    webSocketHttpHeaders.add(SEC_WEB_SOCKET_PROTOCOL, buildValidJwt());
    var uri = URI.create("ws://localhost:%s/notifications".formatted(port));
    var futureSession = webSocketClient.execute(webSocketHandler, webSocketHttpHeaders, uri);
    try {
      currentSession = futureSession.get();
    } catch (InterruptedException | ExecutionException e) {

      throw new RuntimeException("Something went wrong when connecting to server", e);
    }
  }

  @SneakyThrows
  public void disconnect() {

    if (currentSession != null) {

      currentSession.close();
      currentSession = null;
    }
  }

  @SneakyThrows
  public void sentMessage(String message, String targetUserId) {

    if (currentSession != null) {

      TextMessage textMessage = new TextMessage(buildJsonMessage(message, targetUserId));
      currentSession.sendMessage(textMessage);
    }
  }

  public List<RedirectMessageServiceImpl.InputChatMessageDto> getReceivedMessages() {

    return this.webSocketHandler.getReceivedMessages();
  }

  private String buildValidJwt() {
    var key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(JWT_KEY));
    return Jwts.builder()
            .subject(id)
            .signWith(key)
            .issuedAt(getIssuedAtDate())
            .expiration(getExpirationDate())
            .claim("lqu", username)
            .compact();
  }

  private Date getIssuedAtDate() {

    return new Date(LocalDateTime.now().toInstant(ZoneOffset.UTC).toEpochMilli());
  }

  private Date getExpirationDate() {

    return new Date(LocalDateTime.now().plusDays(5).toInstant(ZoneOffset.UTC).toEpochMilli());
  }

  private String buildJsonMessage(String message, String targetUserId) {

    return GSON_SERIALIZER.toJson(new OutputMessage(targetUserId, message, LocalDateTime.now().format(DATE_TIME_FORMATTER)));
  }

  private record OutputMessage(String targetIdUser, String content, String date) {

  }

  @Getter
  private static class TestWebSocketHandler implements WebSocketHandler {

    private List<RedirectMessageServiceImpl.InputChatMessageDto> receivedMessages = new ArrayList<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

      if (message instanceof TextMessage textMessage) {

        receivedMessages.add(GSON_DESERIALIZER.fromJson(textMessage.getPayload(), RedirectMessageServiceImpl.InputChatMessageDto.class));
      }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

    }

    @Override
    public boolean supportsPartialMessages() {
      return false;
    }
  }

  private static class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    @Override
    public JsonElement serialize(LocalDateTime localDateTime, Type srcType,
                                 JsonSerializationContext context) {

      return new JsonPrimitive(localDateTime.toString());
    }

    @Override
    public LocalDateTime deserialize(JsonElement json, Type typeOfT,
                                     JsonDeserializationContext context) throws JsonParseException {

      return LocalDateTime.parse(json.getAsString());
    }
  }
}
