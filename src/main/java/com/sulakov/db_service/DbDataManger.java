package com.sulakov.db_service;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DbDataManger {
    private static final Logger logger = Logger.getLogger(DbDataManger.class);
    private static final ConnectionProvider CONNECTION_PROVIDER = new ConnectionProvider();

    private DbDataManger() {
    }

    public static void saveUser(int tgrm_id, String first_name, String last_name, String user_name) {
        try (Connection connection = CONNECTION_PROVIDER.getDbConnection()) {
            int rowCount;
            String sql = "update users\n" +
                    "        set first_name   = ?\n" +
                    "           ,last_name    = ?\n" +
                    "           ,user_name    = ?\n" +
                    "           ,lastdate     = sysdate()\n" +
                    "      where tgrm_user_id = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, first_name);
            statement.setString(2, last_name);
            statement.setString(3, user_name);
            statement.setInt(4, tgrm_id);

            rowCount = statement.executeUpdate();
            if (rowCount == 0) {
                sql = "insert into users\n" +
                      " (tgrm_user_id\n" +
                      " ,first_name\n" +
                      " ,last_name\n" +
                      " ,user_name\n" +
                      " ,lastdate\n" +
                      " )\n" +
                      " values\n" +
                      " (\n" +
                      " ?\n" +
                      " ,?\n" +
                      " ,?\n" +
                      " ,?\n" +
                      " ,sysdate()\n" +
                      " )";
                PreparedStatement preparedStatement = connection.prepareStatement(sql);
                preparedStatement.setInt(1, tgrm_id);
                preparedStatement.setString(2, first_name);
                preparedStatement.setString(3, last_name);
                preparedStatement.setString(4, user_name);
                rowCount = preparedStatement.executeUpdate();
                if (rowCount == 0) {
                    logger.error("[DB] Error adding user");
                }
            }
        } catch (SQLException e) {
            logger.error("[DB] Error while save user data");
        }
    }
}
