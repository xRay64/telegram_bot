package com.sulakov.tbot;

import javafx.util.Pair;

public class ParsedCommand {
    private Command command = Command.NONE;
    private String commandText;

    public ParsedCommand(Command command, String commandText) {
        this.command = command;
        this.commandText = commandText;
    }

    public Pair<Command, String> getCommandTextPair() {
        return new Pair<Command, String>(command, commandText);
    }

    public Command getCommand() {
        return command;
    }

    public void setCommand(Command command) {
        this.command = command;
    }

    public String getCommandText() {
        return commandText;
    }

    public void setCommandText(String commandText) {
        this.commandText = commandText;
    }
}
