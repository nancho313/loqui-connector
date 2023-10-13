package com.nancho313.loqui.connector.boot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.nancho313.loqui.connector")
public class ConnectorApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConnectorApplication.class, args);
    }

}
