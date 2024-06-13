package com.nancho313.loqui.connector.domain.test.vo;

import com.nancho313.loqui.connector.domain.vo.TextMessage;
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

class TextMessageTest {

  @Test
  void createOk() {

    // Arrange
    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the content of the message";
    var connector = UUID.randomUUID().toString();
    var date = LocalDateTime.now().minusDays(1);

    // Act
    var result = TextMessage.create(senderUser, targetUser, content, connector, date);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.senderUser()).isEqualTo(senderUser);
    assertThat(result.targetUser()).isEqualTo(targetUser);
    assertThat(result.content()).isEqualTo(content);
    assertThat(result.connector()).isEqualTo(connector);
    assertThat(result.date()).isEqualTo(date);
  }

  @MethodSource("getCreateInvalidInputData")
  @ParameterizedTest
  void createWithInvalidData(String senderUser, String targetUser, String content, String connector, LocalDateTime date, String expectedErrorMessage) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> TextMessage.create(senderUser, targetUser, content, connector, date));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  public static Stream<Arguments> getCreateInvalidInputData() {

    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the content of the message";
    var connector = UUID.randomUUID().toString();
    var date = LocalDateTime.now().minusDays(1);

    var expectedErrorMessage1 = "The sender user cannot be empty.";
    var expectedErrorMessage2 = "The target user cannot be empty.";
    var expectedErrorMessage3 = "The content cannot be empty.";
    var expectedErrorMessage4 = "The connector cannot be empty.";
    var expectedErrorMessage5 = "The date cannot be null.";

    return Stream.of(
        Arguments.of(null, targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of("", targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of("  ", targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of(senderUser, null, content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, "", content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, "  ", content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, targetUser, null, connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, "", connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, "  ", connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, content, null, date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, "", date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, "  ", date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, connector, null, expectedErrorMessage5)
    );
  }

  @MethodSource("getBuildObjectInvalidData")
  @ParameterizedTest
  void buildObjectWithInvalidData(String senderUser, String targetUser, String content, String connector,
                                  LocalDateTime date, String expectedErrorMessage) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> new TextMessage(senderUser, targetUser, content, connector, date));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  public static Stream<Arguments> getBuildObjectInvalidData() {

    var senderUser = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the content of the message";
    var connector = UUID.randomUUID().toString();
    var date = LocalDateTime.now();

    var expectedErrorMessage1 = "The sender user cannot be empty.";
    var expectedErrorMessage2 = "The target user cannot be empty.";
    var expectedErrorMessage3 = "The content cannot be empty.";
    var expectedErrorMessage4 = "The connector cannot be empty.";
    var expectedErrorMessage5 = "The date cannot be null";

    return Stream.of(
        Arguments.of(null, targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of("", targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of("  ", targetUser, content, connector, date, expectedErrorMessage1),
        Arguments.of(senderUser, null, content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, "", content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, "  ", content, connector, date, expectedErrorMessage2),
        Arguments.of(senderUser, targetUser, null, connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, "", connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, "  ", connector, date, expectedErrorMessage3),
        Arguments.of(senderUser, targetUser, content, null, date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, "", date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, "  ", date, expectedErrorMessage4),
        Arguments.of(senderUser, targetUser, content, connector, null, expectedErrorMessage5)
    );
  }
}
