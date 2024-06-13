package com.nancho313.loqui.connector.test.application.commandhandler.userconnection;

import com.nancho313.loqui.connector.application.command.userconnection.command.SendTextMessageCommand;
import com.nancho313.loqui.connector.application.command.userconnection.handler.SendTextMessageCommandHandler;
import com.nancho313.loqui.connector.application.exception.InvalidCommandDataException;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.test.application.util.EventResolverFactorySpy;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SendTextMessageCommandHandlerTest {

  private RedirectMessageService redirectMessageServiceMock;

  private EventResolverFactorySpy eventResolverFactory;

  private SendTextMessageCommandHandler sut;

  @BeforeEach
  void setup() {

    var validator = Validation.buildDefaultValidatorFactory().getValidator();
    eventResolverFactory = new EventResolverFactorySpy();
    redirectMessageServiceMock = mock(RedirectMessageService.class);
    sut = new SendTextMessageCommandHandler(validator, eventResolverFactory, redirectMessageServiceMock);
  }

  @Test
  void handleOk() {

    // Arrange
    var userId = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var connector = UUID.randomUUID().toString();
    var date = LocalDateTime.now().minusDays(1);
    var command = new SendTextMessageCommand(userId, targetUser, content, connector, date);
    when(redirectMessageServiceMock.sendMessage(any())).thenReturn(Boolean.TRUE);

    // Act
    sut.handle(command);

    // Assert
    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageServiceMock).sendMessage(argCaptor.capture());
    var capturedValue = argCaptor.getValue();
    assertThat(capturedValue.targetUser()).isEqualTo(targetUser);
    assertThat(capturedValue.senderUser()).isEqualTo(userId);
    assertThat(capturedValue.content()).isEqualTo(content);
    assertThat(capturedValue.connector()).isEqualTo(connector);
    assertThat(capturedValue.date()).isEqualTo(date);
  }

  @Test
  void handleThrowsExceptionWhenProcessingNullCommand() {

    // Arrange
    SendTextMessageCommand command = null;

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).isEqualTo("The command to process cannot be null.");
  }

  @MethodSource("getInvalidData")
  @ParameterizedTest
  void handleInvalidData(String userId, String targetUser, String connector, String content, LocalDateTime date, String expectedErrorMessage) {

    // Arrange
    var command = new SendTextMessageCommand(userId, targetUser, content, connector, date);

    // Act & Assert
    var exception = assertThrows(InvalidCommandDataException.class, () -> sut.handle(command));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);
  }

  private static Stream<Arguments> getInvalidData() {

    var userId = UUID.randomUUID().toString();
    var targetUser = UUID.randomUUID().toString();
    var connector = UUID.randomUUID().toString();
    var content = "This is the message to send";
    var date = LocalDateTime.now().minusDays(1);


    String expectedErrorMessage1 = "The user id cannot be empty.";
    String expectedErrorMessage2 = "The target user cannot be empty.";
    String expectedErrorMessage3 = "The connector id cannot be empty.";
    String expectedErrorMessage4 = "The content cannot be empty.";
    String expectedErrorMessage5 = "The date cannot be null.";

    return Stream.of(
            Arguments.of(null, targetUser, connector, content, date, expectedErrorMessage1),
            Arguments.of("", targetUser, connector, content, date, expectedErrorMessage1),
            Arguments.of("  ", targetUser, connector, content, date, expectedErrorMessage1),
            Arguments.of(userId, null, connector, content, date, expectedErrorMessage2),
            Arguments.of(userId, "", connector, content, date, expectedErrorMessage2),
            Arguments.of(userId, "  ", connector, content, date, expectedErrorMessage2),
            Arguments.of(userId, targetUser, null, content, date, expectedErrorMessage3),
            Arguments.of(userId, targetUser, "", content, date, expectedErrorMessage3),
            Arguments.of(userId, targetUser, "  ", content, date, expectedErrorMessage3),
            Arguments.of(userId, targetUser, connector, null, date, expectedErrorMessage4),
            Arguments.of(userId, targetUser, connector, "", date, expectedErrorMessage4),
            Arguments.of(userId, targetUser, connector, "  ", date, expectedErrorMessage4),
            Arguments.of(userId, targetUser, connector, content, null, expectedErrorMessage5)
    );
  }
}
