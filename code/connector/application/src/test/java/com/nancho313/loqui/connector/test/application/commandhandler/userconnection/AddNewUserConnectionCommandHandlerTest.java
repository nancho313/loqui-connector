package com.nancho313.loqui.connector.test.application.commandhandler.userconnection;

import com.nancho313.loqui.connector.application.command.userconnection.command.AddNewUserConnectionCommand;
import com.nancho313.loqui.connector.application.command.userconnection.handler.AddNewUserConnectionCommandHandler;
import com.nancho313.loqui.connector.application.exception.InvalidCommandDataException;
import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
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
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AddNewUserConnectionCommandHandlerTest {

  private final String connectorId = UUID.randomUUID().toString();

  private EventResolverFactorySpy eventResolverFactory;

  private UserConnectionRepositorySpy repository;

  private AddNewUserConnectionCommandHandler sut;

  @BeforeEach
  void setup() {

    var validator = Validation.buildDefaultValidatorFactory().getValidator();
    eventResolverFactory = new EventResolverFactorySpy();
    repository = new UserConnectionRepositorySpy();
    sut = new AddNewUserConnectionCommandHandler(validator, eventResolverFactory, repository, connectorId);
  }

  @Test
  void handleOk() {

    // Arrange
    var connectionId = UUID.randomUUID().toString();
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var command = new AddNewUserConnectionCommand(connectionId, idUser, username);

    // Act
    sut.handle(command);

    // Assert
    var userConnection = repository.findById(UserConnectionId.of(connectionId));
    assertThat(userConnection).isNotNull().isPresent();
    assertThat(userConnection.get().getId().id()).isEqualTo(connectionId);
    assertThat(userConnection.get().getIdUser()).isEqualTo(idUser);
    assertThat(userConnection.get().getUsername()).isEqualTo(username);
    assertThat(userConnection.get().getStatus()).isEqualTo(UserConnectionStatus.AVAILABLE);
    assertThat(userConnection.get().getCurrentDate().creationDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
    assertThat(userConnection.get().getCurrentDate().lastUpdatedDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));

    var processedEvents = eventResolverFactory.getProcessedEvents();
    assertThat(processedEvents).isNotNull().hasSize(1).allMatch(event -> event instanceof UserConnection.ConnectedUserConnectionEvent);
    var processedEvent = (UserConnection.ConnectedUserConnectionEvent) processedEvents.getFirst();
    assertThat(processedEvent.connectorId()).isEqualTo(connectorId);
    assertThat(processedEvent.idUser()).isEqualTo(idUser);
    assertThat(processedEvent.id().id()).isEqualTo(connectionId);
    assertThat(processedEvent.username()).isEqualTo(username);
    assertThat(processedEvent.date()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
  }

  @Test
  void handleThrowsExceptionWhenProcessingNullCommand() {

    // Arrange
    AddNewUserConnectionCommand command = null;

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).isEqualTo("The command to process cannot be null.");
  }

  @MethodSource("getInvalidData")
  @ParameterizedTest
  void handleInvalidData(String connectionId, String idUser, String username, String expectedErrorMessage) {

    // Arrange
    var command = new AddNewUserConnectionCommand(connectionId, idUser, username);

    // Act & Assert
    var exception = assertThrows(InvalidCommandDataException.class, ()-> sut.handle(command));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  private static Stream<Arguments> getInvalidData() {

    var connectionId = UUID.randomUUID().toString();
    var idUser = UUID.randomUUID().toString();
    var username = "foo";

    String expectedErrorMessage1 = "The connection id cannot be empty.";
    String expectedErrorMessage2 = "The user id cannot be empty.";
    String expectedErrorMessage3 = "The username cannot be empty.";

    return Stream.of(
            Arguments.of(null, idUser, username, expectedErrorMessage1),
            Arguments.of("", idUser, username, expectedErrorMessage1),
            Arguments.of("  ", idUser, username, expectedErrorMessage1),
            Arguments.of(connectionId, null, username, expectedErrorMessage2),
            Arguments.of(connectionId, "", username, expectedErrorMessage2),
            Arguments.of(connectionId, "  ", username, expectedErrorMessage2),
            Arguments.of(connectionId, idUser, null, expectedErrorMessage3),
            Arguments.of(connectionId, idUser, "", expectedErrorMessage3),
            Arguments.of(connectionId, idUser, "  ", expectedErrorMessage3)
    );
  }
}
