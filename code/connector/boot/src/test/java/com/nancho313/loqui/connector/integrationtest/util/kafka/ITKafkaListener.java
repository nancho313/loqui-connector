package com.nancho313.loqui.connector.integrationtest.util.kafka;

import org.springframework.kafka.annotation.KafkaListener;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@KafkaListener(groupId = "integration-test", clientIdPrefix = "integration-test")
public @interface ITKafkaListener {

  String[] topics() default {};
}
