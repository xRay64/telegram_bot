package com.sulakov.tbot;

import javafx.util.Pair;
import org.apache.log4j.Logger;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    private static final Logger logger = Logger.getLogger(CommandParser.class);
    private final String PREFIX_FOR_COMMAND = "/";
    private final String COMMAND_BOTNAME_DELIMETER = "@";
    private String botName;

    public CommandParser(String botName) {
        this.botName = botName;
    }

    public ParsedCommand getParsedCommand(String messageText) {
        if (!isCommand(messageText)) {
            return new ParsedCommand(Command.NONE, messageText);
        } else {
            Pair<String, String> commandAndText = getDelimitedCommandFromText(messageText);
            Command definedCommand = getDefinedComandType(commandAndText.getKey());
            return new ParsedCommand(definedCommand, commandAndText.getValue());
        }
    }

    private boolean isCommand(String inputText) {
        if (!"".equals(inputText)) {
            return inputText.trim().startsWith(PREFIX_FOR_COMMAND);
        } else return false;
    }

    /*private*/ Pair<String, String> getDelimitedCommandFromText(String inputText) {
        Pattern pattern = Pattern.compile("^" + PREFIX_FOR_COMMAND + "\\w+(" + COMMAND_BOTNAME_DELIMETER + "{1}" + botName + ")?\\b", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputText);
        if (matcher.find()) {
            String str = inputText.substring(0, matcher.end());
            if (str.indexOf('@') > 0) {
                return new Pair<String, String>(str.substring(1, str.indexOf('@')), inputText.substring(matcher.end()));
            } else {
                return new Pair<String, String>(str.substring(1), inputText.substring(matcher.end()).trim());
            }
        } else {
            return null;
        }
    }

    private Command getDefinedComandType(String command) {
        Command result = null;
        logger.debug(command.toLowerCase()); //TO_DO
        switch (command.toLowerCase()) {
            case "start":
                result = Command.START;
                break;
            case "covid_total":
                result = Command.COVID_TOTAL;
                break;
            case "covid_country":
                result = Command.COVID_COUNTRY;
                break;
        }
        return result;
    }
}
