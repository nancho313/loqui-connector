package com.nancho313.loqui.connector.integrationtest.util.kafka;

import org.springframework.kafka.annotation.KafkaHandler;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@KafkaHandler(isDefault = true)
public @interface ITKafkaHandler {
}
