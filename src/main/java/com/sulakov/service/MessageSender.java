package com.sulakov.service;

import com.sulakov.tbot.Bot;
import org.apache.log4j.Logger;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendSticker;
import org.telegram.telegrambots.meta.api.objects.Message;

public class MessageSender implements Runnable{
    private static final Logger logger = Logger.getLogger(MessageSender.class);
    private static final int WAIT_FOR_NEW_MESSAGE_DELAY = 1000;

    private Bot bot;

    public MessageSender(Bot bot) {
        this.bot = bot;
    }

    @Override
    public void run() {
        logger.info("[START] MessageSender. Bot class: " + bot);
        while (true) {
            for (Object object = bot.sendQueue.poll(); object != null; object = bot.sendQueue.poll()) {
                logger.debug("Try to send message. Object: " + object.toString());
                send(object);
            }
            try {
                Thread.sleep(WAIT_FOR_NEW_MESSAGE_DELAY);
            } catch (InterruptedException e) {
                logger.error("Interrupt exception caught", e);
            }
        }
    }

    private void send(Object object) {
        try {
            switch (defineMessageType(object)) {
                case EXECUTE:
                    BotApiMethod<Message> message = (BotApiMethod<Message>) object;
                    logger.debug("Use Execute for " + object);
                    bot.execute(message);
                    break;
                    /*Закомментировал до лучших времен*/
//                case STICKER:
//                    SendSticker sendSticker = (SendSticker) object;
//                    logger.debug("Use SendSticker for " + object);
//                    bot.sendSticker(sendSticker);
//                    break;
                case PHOTO:
                    logger.debug("Use sendPhoto for : " + object);
                    bot.execute((SendPhoto) object);
                    break;
                case NOT_DETECTED:
                    logger.warn("Cant detect type of object. " + object);
            }
        } catch (Exception e) {
            logger.error("Cought Exception in MessageSender.send(): " + e);
        }
    }

    private MessageType defineMessageType(Object object) {
        if (object instanceof BotApiMethod) {
            return MessageType.EXECUTE;
        }
        if (object instanceof SendPhoto) {
            return MessageType.PHOTO;
        }
        if (object instanceof SendSticker) {
            return MessageType.STICKER;
        }
        return MessageType.NOT_DETECTED;
    }

    enum MessageType {
        EXECUTE, PHOTO,  STICKER, NOT_DETECTED
    }
}
