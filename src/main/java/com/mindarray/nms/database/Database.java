package com.mindarray.nms.database;

import com.mindarray.nms.httpRequest.HttpListener;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class Database extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    public static final String SELECT = "Select column from table where condition";
    public static final String INSERT = "insert into table columns data ;";
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOGGER.debug("Database Verticle Deployed");
       var connection = connect(); //To connect with database
       var statement = connection.createStatement();
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("Discovery",handler ->{
            JsonObject credentials = new JsonObject();
            credentials = (JsonObject) handler.body();
            String available = "Select exists(Select * from monitor where IP_address= \""+credentials.getString("IP_Address")+"\");";
            try{
                ResultSet resultSet = statement.executeQuery(available);
                int value = 0;
                while(resultSet.next()){
                    value = resultSet.getInt(1);
                }
                if(value==1){
                    LOGGER.debug("IP already discovered");
                    handler.reply("already");
                }else{
                    String query = INSERT.replace("table","monitor").
                            replace("columns","(IP_address,username,Password,Metric_type,Port) values (").
                            replace("data","\""+credentials.getString("IP_Address")+"\",\""+credentials.getString("username")
                                    +"\",\""+credentials.getString("password")+"\",\""+credentials.getString("Metric_Type")+"\","+credentials.getString("Port")+")");
                    statement.execute(query);
                    handler.reply("successful");
                }
            }catch (Exception e){
                LOGGER.debug("Exception Occurred :"+e.getMessage());
                handler.reply(e.getMessage());
            }

        });
    }
    public static Connection  connect() {
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/nms", "vedant.dokania", "Mind@123");
            //Statement stmt = connection.createStatement();
            LOGGER.debug("Database Connection Successful");
        } catch (Exception e) {
            LOGGER.error("Exception Occured :" + e.getMessage());
        }
        return connection;
    }
}
