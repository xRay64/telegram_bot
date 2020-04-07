package com.sulakov.service;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class CovidInfoGetter {
    private static final Logger logger = Logger.getLogger(CovidInfoGetter.class);
    private static final String API_URL = "https://coronavirus-19-api.herokuapp.com/";
    private static final String API_URL_APPENDIX_FOR_ALL_STAT = "all";
    private static final String API_URL_APPENDIX_FOR_COUNTRY = "countries/";

    private CovidInfoGetter() {
    }

    public static String getWorldStatistic() {
        logger.debug("Trying to get JSON from URL: " + API_URL + API_URL_APPENDIX_FOR_ALL_STAT);
        JSONObject jsonObject = getJsonFromUrl(API_URL + API_URL_APPENDIX_FOR_ALL_STAT);
        logger.debug("Get JSON: " + jsonObject.toString());
        return "Мировая статистика COVID-19\n" +
                "Всего случаев: " + String.format("%,d", jsonObject.get("cases")) + "\n" +
                "Всего умерших: " + String.format("%,d", jsonObject.get("deaths")) + "\n" +
                "Всего выздоровивших: " + String.format("%,d", jsonObject.get("recovered"));
    }

    public static String getCountryStatistic(String countryName) {
        if ("".equals(countryName)) {
            return "Нужно указать название страны на английском";
        }
        String urlString = API_URL + API_URL_APPENDIX_FOR_COUNTRY + countryName;
        logger.debug("Trying to get JSON from URL: " + urlString);
        JSONObject jsonObject = getJsonFromUrl(urlString);
        logger.debug("get JSON: " + jsonObject.toString());
        if (jsonObject.toString().trim().equals("Country not found")) {
            return "Не могк опредеить страную. Имя страны нужно вводить по английски.";
        } else {
            return "Cтатистика COVID-19 по " + jsonObject.get("country") + "\n" +
                    "Всего случаев: " + String.format("%,d", jsonObject.get("cases")) + "\n" +
                    "Случаев за сегодня: " + String.format("%,d", jsonObject.get("todayCases")) + "\n" +
                    "Всего умерших: " + String.format("%,d", jsonObject.get("deaths")) + "\n" +
                    "Умерших за сегодня: " + String.format("%,d", jsonObject.get("todayDeaths")) + "\n" +
                    "Выздоровивших: " + String.format("%,d", jsonObject.get("recovered")) + "\n" +
                    "Сейчас боллеет: " + String.format("%,d", jsonObject.get("active")) + "\n" +
                    "В тяжелом состоянии: " + String.format("%,d", jsonObject.get("critical"));
        }

    }

    private static JSONObject getJsonFromUrl(String url) {
        String jsonString = "";
        try {
            URL api = new URL(url);
            URLConnection connection = api.openConnection();
            BufferedReader connectionReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            if (connectionReader.ready()) {
                jsonString = connectionReader.readLine();
            }
            connectionReader.close();
        } catch (IOException e) {
            logger.error("Error while creating an URL object.", e);
        }
        return new JSONObject(jsonString);
    }
}
