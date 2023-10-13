package com.nancho313.loqui.connector.contract.websocket.config;

import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.messaging.SessionConnectedEvent;

@Configuration
public class WebSocketListener implements ApplicationListener<SessionConnectedEvent> {

    @Override
    public void onApplicationEvent(SessionConnectedEvent event) {

        System.out.println("Eventssss");
        System.out.println(event);
    }
}
