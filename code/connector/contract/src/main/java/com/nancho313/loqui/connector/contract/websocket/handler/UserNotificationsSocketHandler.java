package com.nancho313.loqui.connector.contract.websocket.handler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.nancho313.loqui.connector.application.command.CommandHandler;
import com.nancho313.loqui.connector.application.command.userconnection.command.AddNewUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.command.DisconnectUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.command.RedirectMessageCommand;
import com.nancho313.loqui.connector.contract.websocket.dto.OutputChatMessageDto;
import com.nancho313.loqui.connector.websocket.dto.AuthUser;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserNotificationsSocketHandler extends TextWebSocketHandler {
  
  private static final String AUTH_USER = "authUser";
  
  private static final Gson GSON = new GsonBuilder().registerTypeAdapter(LocalDateTime.class,
          (JsonDeserializer<LocalDateTime>) (json, type, jsonDeserializationContext) ->
                  ZonedDateTime.parse(json.getAsJsonPrimitive().getAsString()).toLocalDateTime()).create();
  
  private final WebSocketSessionHub sessionHub;
  
  private final CommandHandler<AddNewUserConnectionCommand> addNewUserConnectionCommandHandler;
  
  private final CommandHandler<DisconnectUserConnectionCommand> disconnectUserConnectionCommandHandler;
  
  private final CommandHandler<RedirectMessageCommand> sendTextMessageCommandHandler;
  
  @Override
  public void handleTextMessage(WebSocketSession session, TextMessage message) {
    
    var chatMessage = toChatMessage(message);
    var command = new RedirectMessageCommand(session.getId(), chatMessage.targetIdUser(), chatMessage.content(),
            chatMessage.date());
    sendTextMessageCommandHandler.handle(command);
  }
  
  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    
    sessionHub.addSession(session);
    var authUser = getAuthUser(session);
    var command = new AddNewUserConnectionCommand(session.getId(), authUser.userId(), authUser.username());
    addNewUserConnectionCommandHandler.handle(command);
  }
  
  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
    
    sessionHub.removeSession(session);
    var command = new DisconnectUserConnectionCommand(session.getId());
    disconnectUserConnectionCommandHandler.handle(command);
  }
  
  private AuthUser getAuthUser(WebSocketSession session) {
    return (AuthUser) session.getAttributes().get(AUTH_USER);
  }
  
  private OutputChatMessageDto toChatMessage(TextMessage message) {
    
    return GSON.fromJson(message.getPayload(), OutputChatMessageDto.class);
  }
}
