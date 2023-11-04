package com.nancho313.loqui.connector.contract.websocket.dto;

import java.time.LocalDateTime;

public record OutputChatMessageDto(String targetIdUser, String content, LocalDateTime date) {
}
