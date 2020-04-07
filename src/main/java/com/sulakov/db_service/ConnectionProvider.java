package com.sulakov.db_service;

import com.sulakov.service.PropertiesManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionProvider {
    private static final Logger logger = Logger.getLogger(ConnectionProvider.class);
    private String dbUrl;
    private String dbUser;
    private String dbPassword;

    public ConnectionProvider() {
        dbUrl = PropertiesManager.getProperty("db.url");
        dbUser = PropertiesManager.getProperty("db.user");
        dbPassword = PropertiesManager.getProperty("db.password");
    }

    public Connection getDbConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, dbUser, dbPassword);
    }
}
