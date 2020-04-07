package com.sulakov.service;

import com.sulakov.db_service.DbDataManger;
import com.sulakov.tbot.Bot;
import com.sulakov.tbot.CommandParser;
import com.sulakov.tbot.ParsedCommand;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;

public class ReceivedUpdateHandler implements Runnable {

    private static final Logger logger = Logger.getLogger(ReceivedUpdateHandler.class);
    private Bot bot;
    private Update update;
    private CommandParser parser;

    public ReceivedUpdateHandler(Bot bot, Update update) {
        this.bot = bot;
        this.update = update;
        parser = new CommandParser(bot.getBotUsername());
    }

    @Override
    public void run() {
        logger.info("Start handling the update: " + update);
        switch (getUpdateType(update)) {
            case MESSAGE_WITH_TEXT:
                Long chatId = update.getMessage().getChatId();
                Boolean needToAnswer = false;
                String textToSend = "";

                //Сохранение данных пользователя в БД
                DbDataManger.saveUser(update.getMessage().getFrom().getId(),
                        update.getMessage().getFrom().getFirstName(),
                        update.getMessage().getFrom().getLastName(),
                        update.getMessage().getFrom().getUserName());

                //получаем текст сообщения и парси его
                String messageText = update.getMessage().getText();
                ParsedCommand parsedCommand = parser.getParsedCommand(messageText);
                //если в тексте есть команда - то реагируем на неё соответствующим образом
                switch (parsedCommand.getCommand()) {
                    case START:
                        textToSend = "Привет, " + update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() +"\n" +
                                "Я бот и пока толком ничего не умею :(\n" +
                                "Сейчас доступны команды:\n" +
                                "/covid_total - получить мировую сводку по короновирусы на текуший момент\n" +
                                "/covid_country {country_name} - получить сводку по короновирусу по указаной стране";
                        needToAnswer = true;
                        break;
                    case COVID_TOTAL:
                        textToSend = CovidInfoGetter.getWorldStatistic();
                        needToAnswer = true;
                        break;
                    case COVID_COUNTRY:
                        textToSend = CovidInfoGetter.getCountryStatistic(parsedCommand.getCommandText());
                        needToAnswer = true;
                        break;
                }

                if (needToAnswer) {
                    SendMessage messageToSend = new SendMessage();
                    messageToSend.setChatId(chatId);
                    messageToSend.setText(textToSend.equals("") ? "Мне нечего тебе сказать!" : textToSend);
                    bot.sendQueue.add(messageToSend);
                }
                break;
        }
    }

    private MessageType getUpdateType(Update update) {
        if (update.hasMessage()) {
            if (update.getMessage().hasText()) {
                return MessageType.MESSAGE_WITH_TEXT;
            }
        }
        return MessageType.NOT_DETECTED;
    }

    private enum MessageType {
        MESSAGE_WITH_TEXT, NOT_DETECTED
    }
}
