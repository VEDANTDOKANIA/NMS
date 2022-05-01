package com.mindarray.nms.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    public static Connection connect() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/", "vedant.dokania", "Mind@123");
            //Statement stmt = connection.createStatement();
            LOGGER.info("Database Connection Successful");
        } catch (Exception e) {
            LOGGER.error("Exception Occured :" + e.getMessage());
        }
        return connection;
    }
}
