package com.nancho313.loqui.connector.test.contract.websocket.handler;

import com.nancho313.loqui.connector.application.command.userconnection.command.AddNewUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.command.DisconnectUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.command.RedirectMessageCommand;
import com.nancho313.loqui.connector.contract.websocket.handler.UserNotificationsSocketHandler;
import com.nancho313.loqui.connector.test.contract.util.CommandHandlerTestUtil;
import com.nancho313.loqui.connector.test.contract.util.TestWebSocketSession;
import com.nancho313.loqui.connector.websocket.service.WebSocketSessionHub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class UserNotificationsSocketHandlerTest {

  private static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  private CommandHandlerTestUtil<AddNewUserConnectionCommand> addNewUserConnectionCommandHandler;

  private CommandHandlerTestUtil<DisconnectUserConnectionCommand> disconnectUserConnectionCommandHandler;

  private CommandHandlerTestUtil<RedirectMessageCommand> sendTextMessageCommandHandler;

  private WebSocketSessionHub sessionHubMock;

  private UserNotificationsSocketHandler sut;

  @BeforeEach
  void setup() {

    addNewUserConnectionCommandHandler = new CommandHandlerTestUtil<>();
    disconnectUserConnectionCommandHandler = new CommandHandlerTestUtil<>();
    sendTextMessageCommandHandler = new CommandHandlerTestUtil<>();
    sessionHubMock = mock(WebSocketSessionHub.class);
    sut = new UserNotificationsSocketHandler(sessionHubMock, addNewUserConnectionCommandHandler, disconnectUserConnectionCommandHandler, sendTextMessageCommandHandler);
  }

  @Test
  void handleTextMessageOk() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var username = "foo";
    var session = TestWebSocketSession.create(id, username);
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var date = LocalDateTime.now().minusDays(1);
    var dateAsString = date.format(DateTimeFormatter.ofPattern(DATE_PATTERN));
    var messageContent = """
            {
              "targetIdUser": "%s",
              "content": "%s",
              "date": "%s"
            }""".formatted(targetUser, content, dateAsString);
    var message = new TextMessage(messageContent);

    // Act
    sut.handleTextMessage(session, message);

    // Assert
    var commandToProcess = sendTextMessageCommandHandler.getCommandToProcess();
    assertThat(commandToProcess).isNotNull();
    assertThat(commandToProcess.connection()).isEqualTo(session.getId());
    assertThat(commandToProcess.targetIdUser()).isEqualTo(targetUser);
    assertThat(commandToProcess.content()).isEqualTo(content);
    assertThat(commandToProcess.date()).isCloseTo(date, within(1, ChronoUnit.SECONDS));
  }

  @Test
  void afterConnectionEstablishedOk() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var username = "foo";
    var session = TestWebSocketSession.create(id, username);

    // Act
    sut.afterConnectionEstablished(session);

    // Assert
    verify(sessionHubMock).addSession(session);

    var commandToProcess = addNewUserConnectionCommandHandler.getCommandToProcess();
    assertThat(commandToProcess).isNotNull();
    assertThat(commandToProcess.connectionId()).isEqualTo(session.getId());
    assertThat(commandToProcess.idUser()).isEqualTo(id);
    assertThat(commandToProcess.username()).isEqualTo(username);
  }

  @Test
  void afterConnectionClosedOk() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var username = "foo";
    var session = TestWebSocketSession.create(id, username);

    // Act
    sut.afterConnectionClosed(session, CloseStatus.NORMAL);

    // Assert
    verify(sessionHubMock).removeSession(session);

    var commandToProcess = disconnectUserConnectionCommandHandler.getCommandToProcess();
    assertThat(commandToProcess).isNotNull();
    assertThat(commandToProcess.connection()).isEqualTo(session.getId());
  }
}
