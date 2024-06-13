package com.nancho313.loqui.connector.integrationtest;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan({"com.nancho313.loqui.connector.integrationtest.util.kafka"})
public class ITConfiguration {

}
