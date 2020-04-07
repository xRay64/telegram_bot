package com.sulakov.service;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private static final Logger logger = Logger.getLogger(PropertiesManager.class);
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(PropertiesManager.class.getClassLoader().getResourceAsStream("config.properties"));
        } catch (IOException e) {
            logger.debug("Caught IOException while loading properties. ", e);
        }
    }

    private PropertiesManager() {
    }

    public static String getProperty(String propertyName) {
        String result = "";
        result = properties.getProperty(propertyName);
        if (result.equals("")) {
            throw new NullPointerException();
        } else {
            return result;
        }
    }
}
