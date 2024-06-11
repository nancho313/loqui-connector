package com.nancho313.loqui.connector.domain.test.vo;

import com.nancho313.loqui.connector.domain.vo.UserConnectionId;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserConnectionIdTest {

  private static final String NULL_STRING = null;

  @Test
  void createOk() {

    // Arrange
    var id = UUID.randomUUID().toString();

    // Act
    var result = UserConnectionId.of(id);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
  }

  @Test
  void buildObjectOk() {

    // Arrange
    var id = UUID.randomUUID().toString();

    // Act
    var result = new UserConnectionId(id);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.id()).isEqualTo(id);
  }

  @NullAndEmptySource
  @ParameterizedTest
  void createObjectWithInvalidData(String value) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> UserConnectionId.of(value));
    assertThat(exception.getMessage()).contains("The id cannot be empty.");
  }

  @NullAndEmptySource
  @ParameterizedTest
  void buildObjectWithInvalidData(String value) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> new UserConnectionId(value));
    assertThat(exception.getMessage()).contains("The id cannot be empty.");
  }
}
