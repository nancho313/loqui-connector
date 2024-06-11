package com.nancho313.loqui.connector.domain.test.aggregate;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.vo.CurrentDate;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.domain.vo.UserConnectionStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserConnectionTest {

  @Test
  void createOk() {

    // Arrange
    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();

    // Act
    var result = UserConnection.create(id, idUser, username, connectorId);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getIdUser()).isEqualTo(idUser);
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getConnectorId()).isEqualTo(connectorId);
    assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.AVAILABLE);
    assertThat(result.getCurrentDate().creationDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
    assertThat(result.getCurrentDate().lastUpdatedDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));

    assertThat(result.isAvailable()).isTrue();

    var events = result.getCurrentEvents();
    assertThat(events).isNotNull().hasSize(1).allMatch(event -> event instanceof UserConnection.ConnectedUserConnectionEvent);
    var event = (UserConnection.ConnectedUserConnectionEvent) events.getFirst();
    assertThat(event.idUser()).isEqualTo(idUser);
    assertThat(event.connectorId()).isEqualTo(connectorId);
    assertThat(event.username()).isEqualTo(username);
    assertThat(event.id()).isEqualTo(id);
    assertThat(event.date()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
  }

  @Test
  void disconnectOk() {

    // Arrange
    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();
    var currentDate = new CurrentDate(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
    var availableUserConnection = new UserConnection(id, idUser, username, connectorId, UserConnectionStatus.AVAILABLE, currentDate);

    // Act
    var result = availableUserConnection.disconnect();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getIdUser()).isEqualTo(idUser);
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getConnectorId()).isEqualTo(connectorId);
    assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.DISCONNECTED);
    assertThat(result.getCurrentDate().creationDate()).isEqualTo(availableUserConnection.getCurrentDate().creationDate());
    assertThat(result.getCurrentDate().lastUpdatedDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));

    assertThat(result.isAvailable()).isFalse();

    var events = result.getCurrentEvents();
    assertThat(events).isNotNull().hasSize(1).allMatch(event -> event instanceof UserConnection.DisconnectedUserConnectionEvent);
    var event = (UserConnection.DisconnectedUserConnectionEvent) events.getFirst();
    assertThat(event.idUser()).isEqualTo(idUser);
    assertThat(event.connectorId()).isEqualTo(connectorId);
    assertThat(event.username()).isEqualTo(username);
    assertThat(event.id()).isEqualTo(id);
    assertThat(event.date()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
  }

  @Test
  void endOk() {

    // Arrange
    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();
    var currentDate = new CurrentDate(LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));
    var availableUserConnection = new UserConnection(id, idUser, username, connectorId, UserConnectionStatus.AVAILABLE, currentDate);

    // Act
    var result = availableUserConnection.end();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getIdUser()).isEqualTo(idUser);
    assertThat(result.getId()).isEqualTo(id);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getConnectorId()).isEqualTo(connectorId);
    assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.ENDED);
    assertThat(result.getCurrentDate().creationDate()).isEqualTo(availableUserConnection.getCurrentDate().creationDate());
    assertThat(result.getCurrentDate().lastUpdatedDate()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));

    assertThat(result.isAvailable()).isFalse();

    var events = result.getCurrentEvents();
    assertThat(events).isNotNull().hasSize(1).allMatch(event -> event instanceof UserConnection.EndedUserConnectionEvent);
    var event = (UserConnection.EndedUserConnectionEvent) events.getFirst();
    assertThat(event.idUser()).isEqualTo(idUser);
    assertThat(event.connectorId()).isEqualTo(connectorId);
    assertThat(event.username()).isEqualTo(username);
    assertThat(event.id()).isEqualTo(id);
    assertThat(event.date()).isCloseTo(LocalDateTime.now(), within(300, ChronoUnit.MILLIS));
  }

  @MethodSource("getCreateInvalidInputData")
  @ParameterizedTest
  void createWithInvalidData(UserConnectionId id, String idUser, String username, String connectorId, String expectedErrorMessage) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> UserConnection.create(id, idUser, username, connectorId));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  public static Stream<Arguments> getCreateInvalidInputData() {

    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();

    var expectedErrorMessage1 = "The id cannot be null.";
    var expectedErrorMessage2 = "The user id cannot be empty.";
    var expectedErrorMessage3 = "The username cannot be empty.";
    var expectedErrorMessage4 = "The connector id cannot be empty.";

    return Stream.of(
        Arguments.of(null, idUser, username, connectorId, expectedErrorMessage1),
        Arguments.of(id, null, username, connectorId, expectedErrorMessage2),
        Arguments.of(id, "", username, connectorId, expectedErrorMessage2),
        Arguments.of(id, "  ", username, connectorId, expectedErrorMessage2),
        Arguments.of(id, idUser, null, connectorId, expectedErrorMessage3),
        Arguments.of(id, idUser, "", connectorId, expectedErrorMessage3),
        Arguments.of(id, idUser, "  ", connectorId, expectedErrorMessage3),
        Arguments.of(id, idUser, username, null, expectedErrorMessage4),
        Arguments.of(id, idUser, username, "", expectedErrorMessage4),
        Arguments.of(id, idUser, username, "  ", expectedErrorMessage4)
    );
  }

  @MethodSource("getBuildObjectWithInvalidData")
  @ParameterizedTest
  void buildObjectWithInvalidData(UserConnectionId id, String idUser, String username, String connectorId,
                                  UserConnectionStatus status, CurrentDate currentDate, String expectedErrorMessage) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class,
        () -> new UserConnection(id, idUser, username, connectorId, status, currentDate));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  public static Stream<Arguments> getBuildObjectWithInvalidData() {

    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();
    var status = UserConnectionStatus.AVAILABLE;
    var currentDate = CurrentDate.now();

    var expectedErrorMessage1 = "The id cannot be null.";
    var expectedErrorMessage2 = "The user id cannot be empty.";
    var expectedErrorMessage3 = "The username cannot be empty.";
    var expectedErrorMessage4 = "The connector id cannot be empty.";
    var expectedErrorMessage5 = "The status cannot be null.";
    var expectedErrorMessage6 = "The current date cannot be null.";

    return Stream.of(
        Arguments.of(null, idUser, username, connectorId, status, currentDate, expectedErrorMessage1),
        Arguments.of(id, null, username, connectorId, status, currentDate, expectedErrorMessage2),
        Arguments.of(id, "", username, connectorId, status, currentDate, expectedErrorMessage2),
        Arguments.of(id, "  ", username, connectorId, status, currentDate, expectedErrorMessage2),
        Arguments.of(id, idUser, null, connectorId, status, currentDate, expectedErrorMessage3),
        Arguments.of(id, idUser, "", connectorId, status, currentDate, expectedErrorMessage3),
        Arguments.of(id, idUser, "  ", connectorId, status, currentDate, expectedErrorMessage3),
        Arguments.of(id, idUser, username, null, status, currentDate, expectedErrorMessage4),
        Arguments.of(id, idUser, username, "", status, currentDate, expectedErrorMessage4),
        Arguments.of(id, idUser, username, "  ", status, currentDate, expectedErrorMessage4),
        Arguments.of(id, idUser, username, connectorId, null, currentDate, expectedErrorMessage5),
        Arguments.of(id, idUser, username, connectorId, status, null, expectedErrorMessage6)
    );
  }

  @MethodSource("getInvalidStatusPermutationInputData")
  @ParameterizedTest
  void invalidStatusPermutation(UserConnection userConnection, Function<UserConnection, UserConnection> permutation) {

    // Act & Assert
    assertThrows(IllegalStateException.class, ()-> permutation.apply(userConnection));
  }

  public static Stream<Arguments> getInvalidStatusPermutationInputData() {

    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var username = "foo";
    var connectorId = UUID.randomUUID().toString();

    var availableUserConnection = UserConnection.create(id, idUser, username, connectorId);
    var disconnectedUserConnection = availableUserConnection.disconnect();
    var endedUserConnection = availableUserConnection.end();

    Function<UserConnection, UserConnection> disconnect = UserConnection::disconnect;
    Function<UserConnection, UserConnection> end = UserConnection::end;

    return Stream.of(
        Arguments.of(disconnectedUserConnection, disconnect),
        Arguments.of(disconnectedUserConnection, end),
        Arguments.of(endedUserConnection, disconnect),
        Arguments.of(endedUserConnection, end)
    );
  }
}
