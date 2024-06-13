package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;
import jakarta.validation.constraints.NotBlank;

public record AddNewUserConnectionCommand(@NotBlank(message = "The connection id cannot be empty.") String connectionId,
                                          @NotBlank(message = "The user id cannot be empty.") String idUser,
                                          @NotBlank(message = "The username cannot be empty.") String username) implements Command {
}
