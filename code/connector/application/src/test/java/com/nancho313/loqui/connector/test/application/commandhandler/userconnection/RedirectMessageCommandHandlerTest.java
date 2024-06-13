package com.nancho313.loqui.connector.test.application.commandhandler.userconnection;

import com.nancho313.loqui.connector.application.command.userconnection.command.RedirectMessageCommand;
import com.nancho313.loqui.connector.application.command.userconnection.handler.RedirectMessageCommandHandler;
import com.nancho313.loqui.connector.application.exception.InvalidCommandDataException;
import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.domainservice.MessageResolver;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.test.application.util.EventResolverFactorySpy;
import com.nancho313.loqui.connector.test.application.util.UserConnectionRepositorySpy;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RedirectMessageCommandHandlerTest {

  private final String connectorId = UUID.randomUUID().toString();

  private EventResolverFactorySpy eventResolverFactory;

  private UserConnectionRepositorySpy repository;

  private RedirectMessageCommandHandler sut;

  private RedirectMessageService redirectMessageServiceMock;

  @BeforeEach
  void setup() {

    var validator = Validation.buildDefaultValidatorFactory().getValidator();
    eventResolverFactory = new EventResolverFactorySpy();
    repository = new UserConnectionRepositorySpy();
    redirectMessageServiceMock = mock(RedirectMessageService.class);
    sut = new RedirectMessageCommandHandler(validator, eventResolverFactory, repository, redirectMessageServiceMock);
  }

  @Test
  void handleOk() {

    // Arrange
    var userConnection1 = buildUserConnection(UUID.randomUUID().toString(), "foo1");
    var userConnection2 = buildUserConnection(UUID.randomUUID().toString(), "foo2");
    repository.save(userConnection1);
    repository.save(userConnection2);
    var content = "This is the message to send";
    var date = LocalDateTime.now();
    var command = new RedirectMessageCommand(userConnection1.getId().id(), userConnection2.getIdUser(), content, date);
    when(redirectMessageServiceMock.redirectMessage(any())).thenReturn(Boolean.TRUE);

    // Act
    sut.handle(command);

    // Assert
    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageServiceMock).redirectMessage(argCaptor.capture());
    var capturedValue = argCaptor.getValue();
    assertThat(capturedValue.senderUser()).isEqualTo(userConnection1.getIdUser());
    assertThat(capturedValue.targetUser()).isEqualTo(userConnection2.getIdUser());
    assertThat(capturedValue.content()).isEqualTo(content);
    assertThat(capturedValue.connector()).isEqualTo(connectorId);
    assertThat(capturedValue.date()).isEqualTo(date);

    var processedEvents = eventResolverFactory.getProcessedEvents();
    assertThat(processedEvents).isNotNull().hasSize(1).allMatch(event -> event instanceof MessageResolver.RedirectedTextMessageEvent);
    var processedEvent = (MessageResolver.RedirectedTextMessageEvent) processedEvents.getFirst();
    assertThat(processedEvent.message().senderUser()).isEqualTo(userConnection1.getIdUser());
    assertThat(processedEvent.message().targetUser()).isEqualTo(userConnection2.getIdUser());
    assertThat(processedEvent.message().content()).isEqualTo(content);
    assertThat(processedEvent.message().connector()).isEqualTo(connectorId);
    assertThat(processedEvent.message().date()).isEqualTo(date);
  }

  @Test
  void handleButNoMessageWasRedirected() {

    // Arrange
    var userConnection1 = buildUserConnection(UUID.randomUUID().toString(), "foo1");
    var userConnection2 = buildUserConnection(UUID.randomUUID().toString(), "foo2");
    repository.save(userConnection1);
    repository.save(userConnection2);
    var content = "This is the message to send";
    var date = LocalDateTime.now();
    var command = new RedirectMessageCommand(userConnection1.getId().id(), userConnection2.getIdUser(), content, date);
    when(redirectMessageServiceMock.redirectMessage(any())).thenReturn(Boolean.FALSE);

    // Act
    sut.handle(command);

    // Assert
    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageServiceMock).redirectMessage(argCaptor.capture());
    var capturedValue = argCaptor.getValue();
    assertThat(capturedValue.senderUser()).isEqualTo(userConnection1.getIdUser());
    assertThat(capturedValue.targetUser()).isEqualTo(userConnection2.getIdUser());
    assertThat(capturedValue.content()).isEqualTo(content);
    assertThat(capturedValue.connector()).isEqualTo(connectorId);
    assertThat(capturedValue.date()).isEqualTo(date);

    var processedEvents = eventResolverFactory.getProcessedEvents();
    assertThat(processedEvents).isNotNull().isEmpty();
  }

  @Test
  void handleThrowsExceptionDueUserConnectionDoesNotExist() {

    // Arrange
    var userConnection1 = buildUserConnection(UUID.randomUUID().toString(), "foo1");
    var userConnection2 = buildUserConnection(UUID.randomUUID().toString(), "foo2");
    repository.save(userConnection2);
    var content = "This is the message to send";
    var date = LocalDateTime.now();
    var command = new RedirectMessageCommand(userConnection1.getId().id(), userConnection2.getIdUser(), content, date);

    // Act & Assert
    var exception = assertThrows(NoSuchElementException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains("The user connection with id "+userConnection1.getId().id()+" does not exist.");
  }

  @Test
  void handleThrowsExceptionDueThereAreNoSessionsToSendTheMessage() {

    // Arrange
    var userConnection1 = buildUserConnection(UUID.randomUUID().toString(), "foo1");
    var userId2 = UUID.randomUUID().toString();
    repository.save(userConnection1);
    var content = "This is the message to send";
    var date = LocalDateTime.now();
    var command = new RedirectMessageCommand(userConnection1.getId().id(), userId2, content, date);
    when(redirectMessageServiceMock.redirectMessage(any())).thenReturn(Boolean.TRUE);

    // Act
    var exception = assertThrows(NoSuchElementException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains("There are not connections related to the user id "+userId2);
  }

  @Test
  void handleThrowsExceptionWhenProcessingNullCommand() {

    // Arrange
    RedirectMessageCommand command = null;

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).isEqualTo("The command to process cannot be null.");
  }

  @MethodSource("getInvalidData")
  @ParameterizedTest
  void handleInvalidData(String connection, String targetIdUser, String content, LocalDateTime date, String expectedErrorMessage) {

    // Arrange
    var command = new RedirectMessageCommand(connection, targetIdUser, content, date);

    // Act & Assert
    var exception = assertThrows(InvalidCommandDataException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  private static Stream<Arguments> getInvalidData() {

    var connection = UUID.randomUUID().toString();
    var targetIdUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var date = LocalDateTime.now();

    String expectedErrorMessage1 = "The connection id cannot be empty.";
    String expectedErrorMessage2 = "The target user cannot be empty.";
    String expectedErrorMessage3 = "The content cannot be empty.";
    String expectedErrorMessage4 = "The date cannot be null.";

    return Stream.of(
            Arguments.of(null, targetIdUser, content, date, expectedErrorMessage1),
            Arguments.of("", targetIdUser, content, date, expectedErrorMessage1),
            Arguments.of("  ", targetIdUser, content, date, expectedErrorMessage1),
            Arguments.of(connection, null, content, date, expectedErrorMessage2),
            Arguments.of(connection, "", content, date, expectedErrorMessage2),
            Arguments.of(connection, "  ", content, date, expectedErrorMessage2),
            Arguments.of(connection, targetIdUser, null, date, expectedErrorMessage3),
            Arguments.of(connection, targetIdUser, "", date, expectedErrorMessage3),
            Arguments.of(connection, targetIdUser, "  ", date, expectedErrorMessage3),
            Arguments.of(connection, targetIdUser, content, null, expectedErrorMessage4)
    );
  }

  private UserConnection buildUserConnection(String idUser, String username) {

    return UserConnection.create(UserConnectionId.of(UUID.randomUUID().toString()), idUser, username, connectorId);
  }
}
