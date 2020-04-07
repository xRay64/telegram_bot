package com.sulakov.tbot;

import com.sulakov.db_service.DbMetadataCreator;
import com.sulakov.service.MessageSender;
import com.sulakov.service.PropertiesManager;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.ApiContextInitializer;

public class Main {
    private final static Logger logger = Logger.getLogger(Main.class);
    private static final int PRIORITY_FOR_SENDER = 1;
    private static final String BOT_NAME = PropertiesManager.getProperty("bot.name");
    private static final String BOT_TOKEN = PropertiesManager.getProperty("bot.token");

    public static void main(String[] args) {
        ApiContextInitializer.init();
        DbMetadataCreator.checkMetadata();
        Bot SulakovTestBot = new Bot(BOT_NAME, BOT_TOKEN);
        SulakovTestBot.botConnect();

        MessageSender messageSender = new MessageSender(SulakovTestBot);

        Thread senderThread = new Thread(messageSender);
        senderThread.setDaemon(true);
        senderThread.setName("MsgSender");
        senderThread.setPriority(PRIORITY_FOR_SENDER);
        senderThread.start();
    }
}
