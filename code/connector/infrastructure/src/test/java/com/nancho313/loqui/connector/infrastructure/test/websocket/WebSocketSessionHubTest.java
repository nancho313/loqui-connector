package com.nancho313.loqui.connector.infrastructure.test.websocket;

import com.nancho313.loqui.connector.infrastructure.test.util.TestWebSocketSession;
import com.nancho313.loqui.connector.infrastructure.websocket.WebSocketSessionHubImpl;
import com.nancho313.loqui.connector.websocket.dto.AuthUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class WebSocketSessionHubTest {

  private WebSocketSessionHubImpl sut;

  @BeforeEach
  void setup() {

    sut = new WebSocketSessionHubImpl();
  }

  @Test
  void getSessionsByIdUserOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession = TestWebSocketSession.create(userId, username);
    sut.addSession(webSocketSession);

    // Act
    var result = sut.getSessionsByIdUser(userId);

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    var attributes = result.getFirst().getAttributes();
    assertThat(attributes).isNotNull().isNotEmpty();
    var authUser = (AuthUser) attributes.get(TestWebSocketSession.AUTH_USER_KEY);
    assertThat(authUser.userId()).isEqualTo(userId);
    assertThat(authUser.username()).isEqualTo(username);
  }

  @Test
  void getSessionsByIdUserReturnsEmptyData() {

    // Arrange
    var userId = UUID.randomUUID().toString();

    // Act
    var result = sut.getSessionsByIdUser(userId);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void getSessionByIdOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession = TestWebSocketSession.create(userId, username);
    var id = webSocketSession.getId();
    sut.addSession(webSocketSession);

    // Act
    var result = sut.getSessionById(id);

    // Assert
    assertThat(result).isNotNull().isPresent();
    var attributes = result.get().getAttributes();
    assertThat(attributes).isNotNull().isNotEmpty();
    var authUser = (AuthUser) attributes.get(TestWebSocketSession.AUTH_USER_KEY);
    assertThat(authUser.userId()).isEqualTo(userId);
    assertThat(authUser.username()).isEqualTo(username);
  }

  @Test
  void getSessionByIdReturnsEmptyData() {

    // Arrange
    var id = UUID.randomUUID().toString();

    // Act
    var result = sut.getSessionById(id);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void removeSessionOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession = TestWebSocketSession.create(userId, username);
    sut.addSession(webSocketSession);

    // Act
    sut.removeSession(webSocketSession);

    // Assert
    var opSession = sut.getSessionById(webSocketSession.getId());
    assertThat(opSession).isEmpty();
  }

  @Test
  void removeJustOneSessionOfAGivenUserOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession1 = TestWebSocketSession.create(userId, username);
    var webSocketSession2 = TestWebSocketSession.create(userId, username);
    sut.addSession(webSocketSession1);
    sut.addSession(webSocketSession2);

    // Act
    sut.removeSession(webSocketSession1);

    // Assert
    var opSession1 = sut.getSessionById(webSocketSession1.getId());
    assertThat(opSession1).isEmpty();

    var opSession2 = sut.getSessionById(webSocketSession2.getId());
    assertThat(opSession2).isPresent();
  }

  @Test
  void addSessionOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession = TestWebSocketSession.create(userId, username);

    // Act
    sut.addSession(webSocketSession);

    // Assert
    var opSession = sut.getSessionById(webSocketSession.getId());
    assertThat(opSession).isPresent();
  }

  @Test
  void addMultipleSessionsOfTheSameUserOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var username = "foo";
    var webSocketSession1 = TestWebSocketSession.create(userId, username);
    var webSocketSession2 = TestWebSocketSession.create(userId, username);
    sut.addSession(webSocketSession1);

    // Act
    sut.addSession(webSocketSession2);

    // Assert
    var sessions = sut.getSessionsByIdUser(userId);
    assertThat(sessions).isNotNull().hasSize(2);
    assertThat(sessions.stream().map(WebSocketSession::getId)).contains(webSocketSession1.getId(), webSocketSession2.getId());
    assertThat(sessions)
        .allMatch(session -> ((AuthUser) session.getAttributes().get(TestWebSocketSession.AUTH_USER_KEY)).userId().equals(userId));
  }
}
