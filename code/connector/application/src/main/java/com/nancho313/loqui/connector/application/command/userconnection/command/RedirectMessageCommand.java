package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record RedirectMessageCommand(@NotBlank(message = "The connection id cannot be empty.") String connection,
                                     @NotBlank(message = "The target user cannot be empty.") String targetIdUser,
                                     @NotBlank(message = "The content cannot be empty.") String content,
                                     @NotNull(message = "The date cannot be null.") LocalDateTime date) implements Command {
}
