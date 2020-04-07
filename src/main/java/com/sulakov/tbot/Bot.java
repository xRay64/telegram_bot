package com.sulakov.tbot;

import com.sulakov.db_service.DbDataManger;
import com.sulakov.service.ReceivedUpdateHandler;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Bot extends TelegramLongPollingBot {

    private final Logger logger = Logger.getLogger(Bot.class);
    private static final int SENDER_THREAD_PRIORITY = 3;
    private int reconnectPause = 10000;
    private String botName;
    private String botToken;
    public final Queue<Object> sendQueue = new ConcurrentLinkedDeque<>();
    public final Queue<Object> receiveQueue = new ConcurrentLinkedDeque<>();

    @Override
    public void onUpdateReceived(Update update) {
        logger.debug("Message received. Updtae id: " + update.getUpdateId());
        ReceivedUpdateHandler receivedUpdateHandler = new ReceivedUpdateHandler(this, update);
        //Старутем тред обработки входящего сообщения
        Thread updateHandlerThread = new Thread(receivedUpdateHandler);
        updateHandlerThread.setDaemon(true);
        updateHandlerThread.setName("MsgRcvr " + update.getUpdateId());
        updateHandlerThread.setPriority(SENDER_THREAD_PRIORITY);
        updateHandlerThread.start();
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @Override
    public String getBotToken() {
        return botToken;
    }

    @Override
    public void onClosing() {

    }

    public Bot(String botName, String botToken) {
        this.botName = botName;
        this.botToken = botToken;
    }

    public int getReconnectPause() {
        return reconnectPause;
    }

    public void setReconnectPause(int reconnectPause) {
        this.reconnectPause = reconnectPause;
    }

    public void botConnect() {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(this);
        } catch (TelegramApiRequestException e) {
            logger.error("Cant connect. Pause - " + reconnectPause / 1000 + "sec, and try again. Error: " + e.getMessage());
            try {
                Thread.sleep(reconnectPause);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect();
        }
    }
}
