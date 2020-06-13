package com.sulakov.db_service;

import com.sulakov.services.ConnectionProvider;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DbCountryStatsManager {
    private static final Logger logger = Logger.getLogger(DbCountryStatsManager.class);
    private Map<Integer, CountryStats> countryStatsMap = new HashMap<>();

    public DbCountryStatsManager() {
        ConnectionProvider connectionProvider = new ConnectionProvider();
        try (Connection connection = connectionProvider.getDbConnection()) {
            logger.debug("Connected to DB. Start to getting list of country stats.");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "select rnk, name, cases, prcnt" +
                            "       from tbot_db.country_cases_top_ten_mvw");
            while (resultSet.next()) {
                countryStatsMap.put(resultSet.getInt(1), new CountryStats(
                        resultSet.getString(2)
                        , resultSet.getInt(1)
                        , resultSet.getInt(3)
                        , resultSet.getFloat(4)));
            }
            logger.debug("Get from DB and put into collection " + countryStatsMap.size() + " rows");
        } catch (SQLException e) {
            logger.error("Caught SQLException: ", e);
        }
    }

    public Map<Integer, CountryStats> getCountryStatsMap() {
        return countryStatsMap;
    }
}
