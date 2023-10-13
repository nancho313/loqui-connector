package com.nancho313.loqui.connector.contract.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/helloworld")
public class HelloWorldController {

    @PostMapping()
    public ResponseEntity<String> sayHello() {

        return ResponseEntity.ok("Hello");
    }
}
