package com.nancho313.loqui.connector.infrastructure.externalservice;

import com.google.gson.*;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.infrastructure.client.kafka.emitter.LoquiKafkaEmitter;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import com.nancho313.loqui.events.UserNotificationEvent;
import com.nancho313.loqui.events.usernotification.SentTextMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.NoSuchElementException;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedirectMessageServiceImpl implements RedirectMessageService {
  
  private final Gson GSON = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
          new LocalDateTimeTypeAdapter()).create();
  
  private final LoquiKafkaEmitter<UserNotificationEvent> emitter;
  
  private final WebSocketSessionHub sessionHub;
  
  public boolean redirectMessage(TextMessage message) {
    
    var notification = SentTextMessage.newBuilder().setContent(message.content()).build();
    
    var avroMessage =
            UserNotificationEvent.newBuilder()
                    .setConnectorId(message.connector())
                    .setDate(message.date().toInstant(ZoneOffset.UTC).toEpochMilli())
                    .setSourceUser(message.senderUser())
                    .setTargetUser(message.targetUser())
                    .setNotification(notification).build();
    return emitter.sendMessage(avroMessage, new ArrayList<>());
  }
  
  public boolean sendMessage(TextMessage message) {

    var sessions = sessionHub.getSessionsByIdUser(message.targetUser());

    if(sessions.isEmpty()) {
      throw new NoSuchElementException("No session was found for the user id %s.".formatted(message.targetUser()));
    }

    try {

      var jsonToSend = parseMessageToJson(new InputChatMessageDto(message.senderUser(), message.targetUser(),
              message.content(), message.date()));
      var textMessage = new org.springframework.web.socket.TextMessage(jsonToSend);
      sessions.forEach(session -> sendMessageThroughWebSocket(session, textMessage));
      return Boolean.TRUE;
    } catch (Exception e) {
      log.error("Something went wrong while sending message to user through websocket.", e);
      return Boolean.FALSE;
    }
  }
  
  private void sendMessageThroughWebSocket(WebSocketSession session,
                                           org.springframework.web.socket.TextMessage message) {
    
    try {
      session.sendMessage(message);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  private String parseMessageToJson(InputChatMessageDto chatMessage) {
    return GSON.toJson(chatMessage);
  }
  
  public record InputChatMessageDto(String sender, String target, String content, LocalDateTime date) {
  }
  
  protected class LocalDateTimeTypeAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {
    
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
