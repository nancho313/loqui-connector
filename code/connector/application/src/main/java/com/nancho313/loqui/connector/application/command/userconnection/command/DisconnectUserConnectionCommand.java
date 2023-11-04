package com.nancho313.loqui.connector.application.command.userconnection.command;

import com.nancho313.loqui.connector.application.command.Command;

public record DisconnectUserConnectionCommand(String connection) implements Command {
}
