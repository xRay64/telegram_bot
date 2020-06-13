package com.sulakov.service;

import com.sulakov.db_service.CountryStats;
import com.sulakov.db_service.DbCountryStatsManager;
import com.sulakov.services.DbDataManger;
import com.sulakov.tbot.Bot;
import com.sulakov.tbot.CommandParser;
import com.sulakov.tbot.ParsedCommand;
import org.apache.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PiePlot3D;
import org.jfree.data.general.DefaultPieDataset;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Map;

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
        if (getUpdateType(update) == MessageType.MESSAGE_WITH_TEXT) {
            Long chatId = update.getMessage().getChatId();
            boolean needToAnswer = false;
            boolean isPicture = false;
            String textToSend = "";
            InputStream imageIs = null;

            //Сохранение данных пользователя в БД
            DbDataManger.saveUser(update.getMessage().getFrom().getId(),
                    update.getMessage().getFrom().getFirstName(),
                    update.getMessage().getFrom().getLastName(),
                    update.getMessage().getFrom().getUserName());

            //получаем текст сообщения и парсим его
            String messageText = update.getMessage().getText();
            ParsedCommand parsedCommand = parser.getParsedCommand(messageText);
            //если в тексте есть команда - то реагируем на неё соответствующим образом
            switch (parsedCommand.getCommand()) {
                case START:
                    textToSend = "Привет, " + update.getMessage().getFrom().getFirstName() + " " + update.getMessage().getFrom().getLastName() + "\n" +
                            "Я бот и пока толком ничего не умею :(\n" +
                            "Сейчас доступны команды:\n" +
                            "/covid_total - получить мировую сводку по короновирусы на текуший момент\n" +
                            "/covid_country {country_name} - получить сводку по короновирусу по указаной стране. Если страна не выбрана - по Росии.";
                    needToAnswer = true;
                    break;
                case COVID_TOTAL:
                    textToSend = CovidInfoGetter.getWorldStatistic();
                    DbCountryStatsManager dbCountryStatsManager = new DbCountryStatsManager();
                    Map<Integer, CountryStats> countryStatsMap = dbCountryStatsManager.getCountryStatsMap();
                    logger.debug("Get in countryStatsMap");
                    if (countryStatsMap.size() > 0) {
                        DefaultPieDataset pieDataset = new DefaultPieDataset();
                        textToSend = textToSend + "\n  ТОП-10 стран по количеству случев: ";
                        for (Map.Entry<Integer, CountryStats> countryStatsEntry :
                                countryStatsMap.entrySet()) {
                            textToSend = textToSend + "\n" + String.format("%-4d%-15s%-10d",
                                    countryStatsEntry.getKey(), countryStatsEntry.getValue().getName(), countryStatsEntry.getValue().getCases());
                            pieDataset.setValue(countryStatsEntry.getValue().getName(), countryStatsEntry.getValue().getPercentOfAll());
                        }
                        //start graph build via JFreeChart
                        JFreeChart chart = ChartFactory.createPieChart3D(
                                "Количество случаев в мире",
                                pieDataset,
                                false,
                                true,
                                false);
                        chart.setBackgroundPaint(Color.WHITE);
                        PiePlot3D plot = (PiePlot3D) chart.getPlot();
                        plot.setStartAngle( 270 );
                        plot.setForegroundAlpha( 0.60f );
                        plot.setInteriorGap( 0.02 );
                        plot.setBackgroundPaint(Color.white);
                        plot.setLabelBackgroundPaint(Color.white);
                        //end graph guild. Start write into image
                        BufferedImage bufferedImage = chart.createBufferedImage(1280, 960);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(bufferedImage, "gif", os);
                        } catch (IOException e) {
                            logger.error(e);
                        }
                        imageIs = new ByteArrayInputStream(os.toByteArray());
                    }

                    needToAnswer = true;
                    isPicture = true;
                    break;
                case COVID_COUNTRY:
                    textToSend = CovidInfoGetter.getCountryStatistic(parsedCommand.getCommandText());
                    needToAnswer = true;
                    break;
                case PICTURE:
                    textToSend = "";
                    needToAnswer = true;
                    isPicture = true;
                    break;
            }

            if (needToAnswer && !isPicture) {
                SendMessage messageToSend = new SendMessage();
                messageToSend.setChatId(chatId);
                messageToSend.setText(textToSend.equals("") ? "Мне нечего тебе сказать!" : textToSend);
                bot.sendQueue.add(messageToSend);
            } else if (needToAnswer && isPicture) {
                SendPhoto sendPhoto = new SendPhoto();
                sendPhoto.setChatId(chatId)
                        .setPhoto("chart",(ByteArrayInputStream)imageIs)
                        .setCaption(textToSend);
                bot.sendQueue.add(sendPhoto);
            }
        } else if (getUpdateType(update) == MessageType.NOT_DETECTED) {
            logger.error("Update type is NOT_DETECTED");
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
