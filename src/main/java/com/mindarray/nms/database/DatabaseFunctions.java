package com.mindarray.nms.database;

import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseFunctions {
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

    public static JsonObject getCredentials(JsonObject Credential){
        var connection = DatabaseFunctions.connect(); //To connect with database
        var credentials = new JsonObject();
        try {
            var statement = connection.createStatement();
            statement.execute("use nms");
            String query = "select poller.Credential_id , poller.IP_address ,poller.Metric_type ,poller.Metric_group,poller.Scheduled_time, poller.Group_status, monitor.username,monitor.password,monitor.port,monitor.community,monitor.version from monitor, poller where monitor.IP_address = poller.Ip_address and monitor.IP_address=\""+Credential.getString("IP_Address")+"\";";
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()){
                var data = new JsonObject();
                data.put("IP_Address",resultSet.getString(2));
                data.put("Metric_Type",resultSet.getString(3));
                data.put("Metric_Group",resultSet.getString(4));
                data.put("Scheduled_Time", resultSet.getInt(5));
                data.put("Group_status",resultSet.getString(6));
                if(data.getString("Metric_Type").equals("linux") || data.getString("Metric_Type").equals("windows")){
                    data.put("username",resultSet.getString(7));
                    data.put("password",resultSet.getString(8));
                    data.put("Port", resultSet.getInt(9));
                }else{
                    data.put("Port", resultSet.getInt(9));
                    data.put("community",resultSet.getString(10));
                    data.put("version", resultSet.getString(11));
                }
                credentials.put(resultSet.getString(1),data );
            }
        } catch (SQLException e) {
            LOGGER.error(e.getCause().getMessage());
        }
        return credentials;
    }
}
