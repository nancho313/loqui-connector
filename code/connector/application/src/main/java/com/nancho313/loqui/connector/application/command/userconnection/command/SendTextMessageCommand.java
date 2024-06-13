package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record SendTextMessageCommand(@NotBlank(message = "The user id cannot be empty.") String userId,
                                     @NotBlank(message = "The target user cannot be empty.") String targetUser,
                                     @NotBlank(message = "The content cannot be empty.") String content,
                                     @NotBlank(message = "The connector id cannot be empty.") String connector,
                                     @NotNull(message = "The date cannot be null.") LocalDateTime date) implements Command {
}
