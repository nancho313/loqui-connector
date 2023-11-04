package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;

import java.time.LocalDateTime;

public record NotifyNewTextMessageCommand(String senderUser, String targetUser, String content,
                                          LocalDateTime date) implements Command {
  
}
