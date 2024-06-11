package com.nancho313.loqui.connector.infrastructure.websocket;

import com.nancho313.loqui.connector.websocket.dto.AuthUser;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.nancho313.loqui.commons.ObjectValidator.isNull;

@Slf4j
@Component
public class WebSocketSessionHubImpl implements WebSocketSessionHub {

  private static final String AUTH_USER_KEY = "authUser";

  private final Map<String, WebSocketSession> currentSessions;

  private final Map<String, List<WebSocketSession>> indexByIdUser;

  public WebSocketSessionHubImpl() {

    currentSessions = new ConcurrentHashMap<>();
    indexByIdUser = new ConcurrentHashMap<>();
  }

  public List<WebSocketSession> getSessionsByIdUser(String idUser) {

    assert (idUser != null);
    var sessionsByIdUser = indexByIdUser.get(idUser);
    return sessionsByIdUser == null ? Collections.emptyList() : List.copyOf(sessionsByIdUser);
  }

  public Optional<WebSocketSession> getSessionById(String id) {
    assert (id != null);
    return Optional.ofNullable(currentSessions.get(id));
  }

  public void removeSession(WebSocketSession session) {

    assert (session != null);

    currentSessions.remove(session.getId());

    var authUser = (AuthUser) session.getAttributes().get(AUTH_USER_KEY);
    var sessionsByIdUser = indexByIdUser.get(authUser.userId());

    if (isNull(sessionsByIdUser)) {

      log.warn("The session with id {} should have been indexed.", session.getId());
    } else {
      sessionsByIdUser.remove(session);
      if (sessionsByIdUser.isEmpty()) {

        indexByIdUser.remove(authUser.userId());
      }
    }
  }

  public void addSession(WebSocketSession session) {

    assert(session != null);

    currentSessions.put(session.getId(), session);

    var authUser = (AuthUser) session.getAttributes().get(AUTH_USER_KEY);
    var sessionsByIdUser = indexByIdUser.get(authUser.userId());
    if (isNull(sessionsByIdUser)) {

      List<WebSocketSession> sessions = new CopyOnWriteArrayList<>();
      sessions.add(session);
      indexByIdUser.put(authUser.userId(), sessions);
    } else {
      sessionsByIdUser.add(session);
    }
  }
}
