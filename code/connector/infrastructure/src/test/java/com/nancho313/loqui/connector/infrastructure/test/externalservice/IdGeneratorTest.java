package com.nancho313.loqui.connector.infrastructure.test.externalservice;

import com.nancho313.loqui.connector.infrastructure.externalservice.IdGeneratorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IdGeneratorTest {

  private IdGeneratorImpl sut;

  @BeforeEach
  void setup() {

    sut = new IdGeneratorImpl();
  }

  @Test
  void generateId() {

    // Act
    var result = sut.generateId();

    // Assert
    assertThat(result).isNotNull().isNotBlank();
  }
}
