package com.nancho313.loqui.connector.contract.websocket.controller;

import com.nancho313.loqui.connector.contract.websocket.dto.HelloWorldRequestDTO;
import com.nancho313.loqui.connector.contract.websocket.dto.HelloWorldResponseDTO;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class HelloWorldWebSocketController {

    private SimpMessagingTemplate template;

    public HelloWorldWebSocketController(SimpMessagingTemplate template) {
        this.template = template;
    }

//    @MessageMapping("/hello")
//    @SendToUser("/topic/greetings")
//    public HelloWorldResponseDTO helloWorld(Message<HelloWorldRequestDTO> message) throws Exception {
//        System.out.println("a new message" + message);
//        Thread.sleep(1000); // simulated delay
//        return new HelloWorldResponseDTO("This is the content for -> %s".formatted(message.getPayload().name()));
//    }

    @MessageMapping("/hello")
    public void helloWorld(Message<HelloWorldRequestDTO> message) throws InterruptedException {
        System.out.println("a new message from the new method" + message);
        Thread.sleep(1000); // simulated delay
        var response = new HelloWorldResponseDTO("This is the content for -> %s".formatted(message.getPayload().name()));
        template.convertAndSend("/topic/greetings", response);
    }
}
