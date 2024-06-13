package com.nancho313.loqui.connector.test.application.commandhandler.userconnection;

import com.nancho313.loqui.connector.application.command.userconnection.command.DisconnectUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.handler.DisconnectUserConnectionCommandHandler;
import com.nancho313.loqui.connector.application.exception.InvalidCommandDataException;
import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.vo.CurrentDate;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.domain.vo.UserConnectionStatus;
import com.nancho313.loqui.connector.test.application.util.EventResolverFactorySpy;
import com.nancho313.loqui.connector.test.application.util.UserConnectionRepositorySpy;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.NoSuchElementException;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DisconnectUserConnectionCommandHandlerTest {

  private final String connectorId = UUID.randomUUID().toString();

  private EventResolverFactorySpy eventResolverFactory;

  private UserConnectionRepositorySpy repository;

  private DisconnectUserConnectionCommandHandler sut;

  @BeforeEach
  void setup() {

    var validator = Validation.buildDefaultValidatorFactory().getValidator();
    eventResolverFactory = new EventResolverFactorySpy();
    repository = new UserConnectionRepositorySpy();
    sut = new DisconnectUserConnectionCommandHandler(validator, eventResolverFactory, repository);
  }

  @Test
  void handleOk() {

    // Arrange
    var connectionId = UUID.randomUUID().toString();
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var availableUserConnection = new UserConnection(UserConnectionId.of(connectionId), idUser, username, connectorId,
            UserConnectionStatus.AVAILABLE, new CurrentDate(LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1)));
    repository.save(availableUserConnection);
    var command = new DisconnectUserConnectionCommand(connectionId);

    // Act
    sut.handle(command);

    // Assert
    var disconnectedUserConnection = repository.findById(UserConnectionId.of(connectionId));
    assertThat(disconnectedUserConnection).isNotNull().isPresent();
    assertThat(disconnectedUserConnection.get().getId().id()).isEqualTo(connectionId);
    assertThat(disconnectedUserConnection.get().getIdUser()).isEqualTo(idUser);
    assertThat(disconnectedUserConnection.get().getUsername()).isEqualTo(username);
    assertThat(disconnectedUserConnection.get().getCurrentDate().creationDate()).isEqualTo(availableUserConnection.getCurrentDate().creationDate());
    assertThat(disconnectedUserConnection.get().getCurrentDate().lastUpdatedDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
    assertThat(disconnectedUserConnection.get().getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);

    var processedEvents = eventResolverFactory.getProcessedEvents();
    assertThat(processedEvents).isNotNull().hasSize(1).allMatch(event -> event instanceof UserConnection.DisconnectedUserConnectionEvent);
    var processedEvent = (UserConnection.DisconnectedUserConnectionEvent) processedEvents.getFirst();
    assertThat(processedEvent.connectorId()).isEqualTo(connectorId);
    assertThat(processedEvent.idUser()).isEqualTo(idUser);
    assertThat(processedEvent.id().id()).isEqualTo(connectionId);
    assertThat(processedEvent.username()).isEqualTo(username);
    assertThat(processedEvent.date()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
  }

  @Test
  void handleThrowsExceptionWhenConnectionDoesNotExist() {

    // Arrange
    var connectionId = UUID.randomUUID().toString();
    var command = new DisconnectUserConnectionCommand(connectionId);

    // Act & Assert
    var exception = assertThrows(NoSuchElementException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains("The user connection with id " + connectionId + " does not exist.");
  }

  @Test
  void handleThrowsExceptionWhenProcessingNullCommand() {

    // Arrange
    DisconnectUserConnectionCommand command = null;

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).isEqualTo("The command to process cannot be null.");
  }

  @MethodSource("getInvalidData")
  @ParameterizedTest
  void handleInvalidData(String connectionId, String expectedErrorMessage) {

    // Arrange
    var command = new DisconnectUserConnectionCommand(connectionId);

    // Act & Assert
    var exception = assertThrows(InvalidCommandDataException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  private static Stream<Arguments> getInvalidData() {

    String nullString = null;

    String expectedErrorMessage1 = "The connection id cannot be empty.";

    return Stream.of(
            Arguments.of(nullString, expectedErrorMessage1),
            Arguments.of("", expectedErrorMessage1),
            Arguments.of("  ", expectedErrorMessage1)
    );
  }
}
