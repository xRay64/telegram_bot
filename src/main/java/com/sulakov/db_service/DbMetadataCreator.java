package com.sulakov.db_service;

import com.sulakov.services.ConnectionProvider;
import com.sulakov.services.PropertiesManager;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*Клас проверят есть ли все необходимые таблицы в БД
 * если нет - создает их*/
public class DbMetadataCreator {

    private static final Logger logger = Logger.getLogger(DbMetadataCreator.class);

    private DbMetadataCreator() {
    }

    public static void checkMetadata() {
        String dbUrl = PropertiesManager.getProperty("db.url");
        String schemaName = dbUrl.substring(dbUrl.lastIndexOf('/') + 1);
        ConnectionProvider connectionProvider = new ConnectionProvider();
        List<String> listOfSchemaTables = new ArrayList<>();
        List<String> listOfSQLToBeExecute = new ArrayList<>();
        try (Connection connection = connectionProvider.getDbConnection()) {
            logger.debug("Connected to DB. Start to getting list of tables.");
            //Получим список всех таблиц схемы
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "select *\n" +
                            "  from information_schema.tables\n" +
                            " where lower(TABLE_SCHEMA) = '" + schemaName.toLowerCase() + "'");
            while (resultSet.next()) {
                listOfSchemaTables.add(resultSet.getString(3).toLowerCase());
            }
        } catch (SQLException e) {
            logger.error("Error while connecting to DB for collect tables data", e);
        }

        //проверяем таблицы и если нет добавлям sql для их создания
        if (!listOfSchemaTables.contains("users")) {
            logger.debug("There is no USERS table, need to create.");
            listOfSQLToBeExecute.add("CREATE TABLE `users` (\n" +
                    "  `id` int(11) unsigned NOT NULL AUTO_INCREMENT,\n" +
                    "  `tgrm_user_id` int(11) NOT NULL,\n" +
                    "  `first_name` varchar(4000) DEFAULT NULL,\n" +
                    "  `last_name` varchar(4000) DEFAULT NULL,\n" +
                    "  `user_name` varchar(4000) DEFAULT NULL,\n" +
                    "  `lastdate` date DEFAULT NULL,\n" +
                    "  PRIMARY KEY (`id`)\n" +
                    ") ENGINE=InnoDB DEFAULT CHARSET=utf8");
        } else {
            logger.debug("All tables exists in DB. No need to create anything");
        }

        try (Connection connection = connectionProvider.getDbConnection()) {
            for (String sql :
                    listOfSQLToBeExecute) {
                Statement statement = connection.createStatement();
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            logger.error("Error while creating tables in DB", e);
        }
    }
}
