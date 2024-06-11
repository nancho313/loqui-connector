package com.nancho313.loqui.connector.domain.test.vo;

import com.nancho313.loqui.connector.domain.vo.CurrentDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CurrentDateTest {

  @Test
  void buildObjectOk() {

    // Arrange
    var creationDate = LocalDateTime.now();
    var lastUpdatedDate = LocalDateTime.now().plusMinutes(5);

    // Act
    var result = new CurrentDate(creationDate, lastUpdatedDate);

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.creationDate()).isCloseTo(creationDate, within(100, ChronoUnit.MILLIS));
    assertThat(result.lastUpdatedDate()).isCloseTo(lastUpdatedDate, within(100, ChronoUnit.MILLIS));
  }

  @Test
  void buildNowInstance() {

    // Arrange
    var now = LocalDateTime.now();

    // Act
    var result = CurrentDate.now();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.creationDate()).isCloseTo(now, within(100, ChronoUnit.MILLIS));
    assertThat(result.lastUpdatedDate()).isCloseTo(now, within(100, ChronoUnit.MILLIS));
  }

  @Test
  void buildUpdateInstanceOk() {

    // Arrange
    var creationDate = LocalDateTime.now().minusHours(5);
    var lastUpdatedDate = LocalDateTime.now().minusHours(5);
    var currentDate = new CurrentDate(creationDate, lastUpdatedDate);
    var now = LocalDateTime.now();

    // Act
    var result = currentDate.update();

    // Assert
    assertThat(result).isNotNull();
    assertThat(result.creationDate()).isCloseTo(creationDate, within(100, ChronoUnit.MILLIS));
    assertThat(result.lastUpdatedDate()).isCloseTo(now, within(100, ChronoUnit.MILLIS));
  }

  @MethodSource("getInvalidData")
  @ParameterizedTest
  void buildObjectWithInvalidData(LocalDateTime creationDate, LocalDateTime lastUpdatedDate,
                                  String expectedErrorMessage) {

    // Act & Assert
    var exception = assertThrows(IllegalArgumentException.class, () -> new CurrentDate(creationDate, lastUpdatedDate));
    assertThat(exception.getMessage()).contains(expectedErrorMessage);

  }

  public static Stream<Arguments> getInvalidData() {

    var creationDate = LocalDateTime.now();
    var lastUpdatedDate = LocalDateTime.now().plusMinutes(5);

    var expectedErrorMessage1 = "The creation date cannot be null.";
    var expectedErrorMessage2 = "The last updated date cannot be null.";
    var expectedErrorMessage3 = "The creation date cannot be greater than last updated date.";

    return Stream.of(
        Arguments.of(null, lastUpdatedDate, expectedErrorMessage1),
        Arguments.of(creationDate, null, expectedErrorMessage2),
        Arguments.of(creationDate, lastUpdatedDate.minusHours(1), expectedErrorMessage3),
        Arguments.of(creationDate.plusHours(1), lastUpdatedDate, expectedErrorMessage3)
    );
  }
}
