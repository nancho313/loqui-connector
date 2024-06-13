package com.nancho313.loqui.connector.integrationtest;

import com.nancho313.loqui.connector.ConnectorApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ImportTestcontainers(TestContainerConfiguration.class)
@ContextConfiguration(
        initializers = {TestContainerConfiguration.KafkaServerInitializer.class},
        classes = {ConnectorApplication.class, ITConfiguration.class})
public abstract class BaseIntegrationTest {
}
