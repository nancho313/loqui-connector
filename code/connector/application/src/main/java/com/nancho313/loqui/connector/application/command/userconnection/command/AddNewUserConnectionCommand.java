package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;

public record AddNewUserConnectionCommand(String connectionId, String idUser, String username) implements Command {
}
