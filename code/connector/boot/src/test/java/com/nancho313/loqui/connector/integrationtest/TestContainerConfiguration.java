package com.nancho313.loqui.connector.integrationtest;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

@TestConfiguration
public class TestContainerConfiguration {

  @Container
  private static final KafkaContainer kafkaContainer;

  @Container
  private static final RedisContainer redisContainer;
  
  static {
    
    kafkaContainer = new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:6.2.1"))
            .withExposedPorts(9092, 9093).withEmbeddedZookeeper()
            .withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "true");
    
    kafkaContainer.setPortBindings(List.of("9092:9092", "9093:9093"));

    redisContainer = new RedisContainer(DockerImageName.parse("redis:6.2.6"));
    redisContainer.setPortBindings(List.of("6379:6379"));
  }
  
  public static class KafkaServerInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    
    @Override
    public void initialize(final ConfigurableApplicationContext applicationContext) {
      kafkaContainer.start();
      TestPropertyValues.of("spring.kafka.bootstrap-servers=" + kafkaContainer.getBootstrapServers()).applyTo(applicationContext.getEnvironment());
      
    }
  }
  
}
