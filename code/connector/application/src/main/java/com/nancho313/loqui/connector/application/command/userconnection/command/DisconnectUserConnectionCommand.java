package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;
import jakarta.validation.constraints.NotBlank;

public record DisconnectUserConnectionCommand(@NotBlank(message = "The connection id cannot be empty.") String connection) implements Command {
}
