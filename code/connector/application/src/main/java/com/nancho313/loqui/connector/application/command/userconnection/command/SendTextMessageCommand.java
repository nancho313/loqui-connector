package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;

import java.time.LocalDateTime;

public record SendTextMessageCommand(String userId, String targetUser, String content, String connector,
                                     LocalDateTime date) implements Command {
}
