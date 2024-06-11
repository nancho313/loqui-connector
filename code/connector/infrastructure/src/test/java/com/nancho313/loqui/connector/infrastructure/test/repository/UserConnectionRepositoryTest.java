package com.nancho313.loqui.connector.infrastructure.test.repository;

import com.nancho313.loqui.connector.domain.aggregate.UserConnection;
import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import com.nancho313.loqui.connector.domain.vo.UserConnectionStatus;
import com.nancho313.loqui.connector.infrastructure.client.redis.dao.UserConnectionRedisDao;
import com.nancho313.loqui.connector.infrastructure.client.redis.hash.UserConnectionHash;
import com.nancho313.loqui.connector.infrastructure.repository.UserConnectionRepositoryImpl;
import org.junit.jupiter.api.BeforeEach;
import com.nancho313.loqui.connector.infrastructure.mapper.UserConnectionMapperImpl;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserConnectionRepositoryTest {

  private UserConnectionRedisDao daoMock;

  private UserConnectionRepositoryImpl sut;

  @BeforeEach
  void setup() {

    daoMock = mock(UserConnectionRedisDao.class);
    sut = new UserConnectionRepositoryImpl(daoMock, new UserConnectionMapperImpl());
  }

  @Test
  void findByIdOk() {

    // Arrange
    var idUser = UUID.randomUUID().toString();
    var id = UUID.randomUUID().toString();
    var userConnectionId = UserConnectionId.of(id);
    var userConnectionHash = buildUserConnectionHash(id, idUser);
    when(daoMock.findById(id)).thenReturn(Optional.of(userConnectionHash));

    // Act
    var result = sut.findById(userConnectionId);

    // Assert
    assertThat(result).isNotNull().isPresent();
    assertThat(result.get().getId().id()).isEqualTo(userConnectionHash.id());
    assertThat(result.get().getIdUser()).isEqualTo(userConnectionHash.idUser());
    assertThat(result.get().getConnectorId()).isEqualTo(userConnectionHash.connectorId());
    assertThat(result.get().getUsername()).isEqualTo(userConnectionHash.username());
    assertThat(result.get().getStatus()).isEqualTo(UserConnectionStatus.valueOf(userConnectionHash.status()));
    assertThat(result.get().getCurrentDate().creationDate()).isEqualTo(userConnectionHash.creationDate());
    assertThat(result.get().getCurrentDate().lastUpdatedDate()).isEqualTo(userConnectionHash.lastUpdatedDate());
  }

  @Test
  void findByIdReturnsEmptyData() {

    // Arrange
    var id = UUID.randomUUID().toString();
    var userConnectionId = UserConnectionId.of(id);
    when(daoMock.findById(id)).thenReturn(Optional.empty());

    // Act
    var result = sut.findById(userConnectionId);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void findAllAvailableByIdUserOk() {

    // Arrange
    var idUser = UUID.randomUUID().toString();
    var id = UUID.randomUUID().toString();
    var userConnectionHash = buildUserConnectionHash(id, idUser);
    when(daoMock.findByIdUser(idUser)).thenReturn(List.of(userConnectionHash));

    // Act
    var result = sut.findAllAvailableByIdUser(idUser);

    // Assert
    assertThat(result).isNotNull().hasSize(1);
    var user = result.getFirst();
    assertThat(user.getId().id()).isEqualTo(userConnectionHash.id());
    assertThat(user.getIdUser()).isEqualTo(userConnectionHash.idUser());
    assertThat(user.getConnectorId()).isEqualTo(userConnectionHash.connectorId());
    assertThat(user.getUsername()).isEqualTo(userConnectionHash.username());
    assertThat(user.getStatus()).isEqualTo(UserConnectionStatus.valueOf(userConnectionHash.status()));
    assertThat(user.getCurrentDate().creationDate()).isEqualTo(userConnectionHash.creationDate());
    assertThat(user.getCurrentDate().lastUpdatedDate()).isEqualTo(userConnectionHash.lastUpdatedDate());
  }

  @Test
  void findAllAvailableByIdUserReturnsEmptyData() {

    // Arrange
    var idUser = UUID.randomUUID().toString();
    when(daoMock.findByIdUser(idUser)).thenReturn(List.of());

    // Act
    var result = sut.findAllAvailableByIdUser(idUser);

    // Assert
    assertThat(result).isNotNull().isEmpty();
  }

  @Test
  void saveOk() {

    // Arrange
    var connectorId = UUID.randomUUID().toString();
    var username = "foo";
    var idUser = UUID.randomUUID().toString();
    var id = UUID.randomUUID().toString();
    var userConnectionId = UserConnectionId.of(id);
    var userConnection = UserConnection.create(userConnectionId, idUser, username, connectorId);

    var userHash = new UserConnectionHash(userConnection.getId().id(), userConnection.getIdUser(), userConnection.getUsername(),
        userConnection.getConnectorId(), userConnection.getStatus().name(),
        userConnection.getCurrentDate().creationDate(), userConnection.getCurrentDate().lastUpdatedDate());

    when(daoMock.save(any())).thenReturn(userHash);

    // Act
    var result = sut.save(userConnection);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.getId().id()).isEqualTo(id);
    assertThat(result.getUsername()).isEqualTo(username);
    assertThat(result.getIdUser()).isEqualTo(idUser);
    assertThat(result.getConnectorId()).isEqualTo(connectorId);
    assertThat(result.getCurrentDate().creationDate()).isEqualTo(userHash.creationDate());
    assertThat(result.getCurrentDate().lastUpdatedDate()).isEqualTo(userHash.lastUpdatedDate());
    assertThat(result.getStatus()).isEqualTo(UserConnectionStatus.AVAILABLE);

    var argCaptor = ArgumentCaptor.forClass(UserConnectionHash.class);
    verify(daoMock).save(argCaptor.capture());
    var capturedValue = argCaptor.getValue();

    assertThat(capturedValue).isNotNull();
    assertThat(capturedValue.id()).isEqualTo(id);
    assertThat(capturedValue.username()).isEqualTo(username);
    assertThat(capturedValue.idUser()).isEqualTo(idUser);
    assertThat(capturedValue.connectorId()).isEqualTo(connectorId);
    assertThat(capturedValue.creationDate()).isEqualTo(userConnection.getCurrentDate().creationDate());
    assertThat(capturedValue.lastUpdatedDate()).isEqualTo(userConnection.getCurrentDate().lastUpdatedDate());
    assertThat(capturedValue.status()).isEqualTo(UserConnectionStatus.AVAILABLE.name());

  }

  private UserConnectionHash buildUserConnectionHash(String id, String idUser) {
    return new UserConnectionHash(id, idUser, "foo", UUID.randomUUID().toString(), UserConnectionStatus.AVAILABLE.name(), LocalDateTime.now(), LocalDateTime.now());
  }
}
