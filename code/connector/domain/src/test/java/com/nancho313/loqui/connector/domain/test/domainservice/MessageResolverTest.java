package com.nancho313.loqui.connector.domain.test.domainservice;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.domainservice.MessageResolver;
import com.nancho313.loqui.connector.domain.externalservice.RedirectMessageService;
import com.nancho313.loqui.connector.domain.vo.TextMessage;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageResolverTest {

  private RedirectMessageService redirectMessageMock;

  private MessageResolver sut;

  @BeforeEach
  void setup() {

    redirectMessageMock = mock(RedirectMessageService.class);
    sut = new MessageResolver(redirectMessageMock);
  }

  @Test
  void redirectMessageOk() {

    // Arrange
    var senderUser = buildUserConnection("foo");
    var targetUser1 = buildUserConnection("foo1");
    var targetUser2 = buildUserConnection("foo2");
    var date = LocalDateTime.now().minusDays(1);
    List<UserConnection> targetUsers = List.of(targetUser1, targetUser2);
    var content = "This is the content of the message.";
    when(redirectMessageMock.redirectMessage(any())).thenReturn(Boolean.TRUE);

    // Act
    var result = sut.redirectMessage(senderUser, targetUsers, content, date);

    // Assert
    assertThat(result).isNotNull().hasSize(2);
    var sentTextMessages = result.stream().map(MessageResolver.RedirectedTextMessageEvent::message).toList();
    assertThat(sentTextMessages).allMatch(sentTextMessage -> sentTextMessage.content().equals(content) && sentTextMessage.senderUser().equals(senderUser.getIdUser()));

    var sentTextMessageUser1 = sentTextMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser1.getIdUser())).findFirst();
    assertThat(sentTextMessageUser1).isPresent();
    assertThat(sentTextMessageUser1.get().targetUser()).isEqualTo(targetUser1.getIdUser());
    assertThat(sentTextMessageUser1.get().connector()).isEqualTo(targetUser1.getConnectorId());

    var sentTextMessageUser2 = sentTextMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser2.getIdUser())).findFirst();
    assertThat(sentTextMessageUser2).isPresent();
    assertThat(sentTextMessageUser2.get().targetUser()).isEqualTo(targetUser2.getIdUser());
    assertThat(sentTextMessageUser2.get().connector()).isEqualTo(targetUser2.getConnectorId());

    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageMock, times(2)).redirectMessage(argCaptor.capture());
    var redirectedMessages = argCaptor.getAllValues();

    var redirectedMessage1 = redirectedMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser1.getIdUser())).findFirst();
    assertThat(redirectedMessage1).isPresent();
    assertThat(redirectedMessage1.get().targetUser()).isEqualTo(targetUser1.getIdUser());
    assertThat(redirectedMessage1.get().connector()).isEqualTo(targetUser1.getConnectorId());

    var redirectedMessage2 = redirectedMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser2.getIdUser())).findFirst();
    assertThat(redirectedMessage2).isPresent();
    assertThat(redirectedMessage2.get().targetUser()).isEqualTo(targetUser2.getIdUser());
    assertThat(redirectedMessage2.get().connector()).isEqualTo(targetUser2.getConnectorId());
  }

  @Test
  void redirectMessageWithNonAvailableConnectionsOk() {

    // Arrange
    var senderUser = buildUserConnection("foo");
    var targetUser1 = buildUserConnection("foo1");
    var targetUser2 = buildUserConnection("foo2").disconnect();
    List<UserConnection> targetUsers = List.of(targetUser1, targetUser2);
    var content = "This is the content of the message.";
    var date = LocalDateTime.now().minusDays(1);
    when(redirectMessageMock.redirectMessage(any())).thenReturn(Boolean.TRUE);

    // Act
    var result = sut.redirectMessage(senderUser, targetUsers, content, date);

    // Assert
    assertThat(result).isNotNull().hasSize(1);

    var sentTextMessageUser = result.stream().map(MessageResolver.RedirectedTextMessageEvent::message).toList().getFirst();
    assertThat(sentTextMessageUser.targetUser()).isEqualTo(targetUser1.getIdUser());
    assertThat(sentTextMessageUser.connector()).isEqualTo(targetUser1.getConnectorId());
    assertThat(sentTextMessageUser.senderUser()).isEqualTo(senderUser.getIdUser());
    assertThat(sentTextMessageUser.content()).isEqualTo(content);

    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageMock).redirectMessage(argCaptor.capture());
    var redirectedMessage = argCaptor.getValue();

    assertThat(redirectedMessage.targetUser()).isEqualTo(targetUser1.getIdUser());
    assertThat(redirectedMessage.connector()).isEqualTo(targetUser1.getConnectorId());
    assertThat(redirectedMessage.senderUser()).isEqualTo(senderUser.getIdUser());
    assertThat(redirectedMessage.content()).isEqualTo(content);
  }

  @Test
  void redirectMessageWithOnlyNonAvailableConnectionsOk() {

    // Arrange
    var senderUser = buildUserConnection("foo");
    var targetUser1 = buildUserConnection("foo1").disconnect();
    var targetUser2 = buildUserConnection("foo2").disconnect();
    List<UserConnection> targetUsers = List.of(targetUser1, targetUser2);
    var date = LocalDateTime.now().minusDays(1);
    var content = "This is the content of the message.";

    // Act
    var result = sut.redirectMessage(senderUser, targetUsers, content, date);

    // Assert
    assertThat(result).isNotNull().isEmpty();

    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageMock, never()).redirectMessage(argCaptor.capture());
  }

  @Test
  void redirectMessageOkButNoMessageWasRedirected() {

    // Arrange
    var senderUser = buildUserConnection("foo");
    var targetUser1 = buildUserConnection("foo1");
    var targetUser2 = buildUserConnection("foo2");
    List<UserConnection> targetUsers = List.of(targetUser1, targetUser2);
    var content = "This is the content of the message.";
    var date = LocalDateTime.now().minusDays(1);
    when(redirectMessageMock.redirectMessage(any())).thenReturn(Boolean.FALSE);

    // Act
    var result = sut.redirectMessage(senderUser, targetUsers, content, date);

    // Assert
    assertThat(result).isNotNull().isEmpty();

    var argCaptor = ArgumentCaptor.forClass(TextMessage.class);
    verify(redirectMessageMock, times(2)).redirectMessage(argCaptor.capture());
    var redirectedMessages = argCaptor.getAllValues();

    var redirectedMessage1 = redirectedMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser1.getIdUser())).findFirst();
    assertThat(redirectedMessage1).isPresent();
    assertThat(redirectedMessage1.get().targetUser()).isEqualTo(targetUser1.getIdUser());
    assertThat(redirectedMessage1.get().connector()).isEqualTo(targetUser1.getConnectorId());

    var redirectedMessage2 = redirectedMessages.stream()
        .filter(value -> value.targetUser().equals(targetUser2.getIdUser())).findFirst();
    assertThat(redirectedMessage2).isPresent();
    assertThat(redirectedMessage2.get().targetUser()).isEqualTo(targetUser2.getIdUser());
    assertThat(redirectedMessage2.get().connector()).isEqualTo(targetUser2.getConnectorId());
  }

  private UserConnection buildUserConnection(String username) {

    var id = UserConnectionId.of(UUID.randomUUID().toString());
    var idUser = UUID.randomUUID().toString();
    var connectorId = UUID.randomUUID().toString();
    return UserConnection.create(id, idUser, username, connectorId);
  }
}
