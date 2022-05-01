package com.mindarray.nms.database;

import com.mindarray.nms.httpRequest.HttpListener;
import com.mindarray.nms.main.SecureRandomString;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;

public class Database extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);
    public static final String SELECT = "Select column from table where condition";
    public static final String INSERT = "insert into table columns data ;";
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOGGER.info("Database Verticle Deployed");
       var connection = DatabaseCommand.connect(); //To connect with database
       var statement = connection.createStatement();
        EventBus eventBus = vertx.eventBus();
        eventBus.consumer("check",handler ->{
            JsonObject credentials = new JsonObject();
            credentials = (JsonObject) handler.body();
            String available ="Select exists(Select * from monitor where IP_address= "+"\""+credentials.getString("IP_Address")+"\");";
            try{
                statement.execute("use nms;");
                ResultSet resultSet = statement.executeQuery(available);
                int value = 0;
                while(resultSet.next()){
                    value = resultSet.getInt(1);
                }
                if(value==1){
                    LOGGER.info("IP already discovered");
                    handler.reply("already");
                }else{
                    LOGGER.info("IP not discovered");
                    handler.reply("not already");
                }
            }catch (Exception e){
                LOGGER.info("Exception Occurred :"+e.getMessage());
                handler.reply(e.getMessage());
            }
        });
        eventBus.consumer("Discovery",handler ->{
            JsonObject credentials = new JsonObject();
            credentials = (JsonObject) handler.body();
            try{
                statement.execute("use nms;");
                String query = INSERT.replace("table","monitor").
                        replace("columns","(IP_address,username,Password,Metric_type,Port) values (").
                        replace("data","\""+credentials.getString("IP_Address")+"\",\""+credentials.getString("username")
                                +"\",\""+credentials.getString("password")+"\",\""+credentials.getString("Metric_Type")+"\","+credentials.getString("Port")+")");
                var flag= statement.execute(query);
                ResultSet rs = statement.executeQuery("Select Metric_group ,time from metric where Metric_type="+"\"" +credentials.getString("Metric_Type")+"\";");
                ArrayList<String> queries= new ArrayList<>();
                while(rs.next()){
                   String value =("insert into poller values(\""+SecureRandomString.generate()+"\",\""+credentials.getString("IP_Address")+"\",\""+
                            credentials.getString("Metric_Type")+"\",\"" +rs.getString(1)+"\","+rs.getInt(2)+",\"enable\");" );
                   queries.add(value);
                }
                queries.forEach( msg ->{
                    try {
                        statement.execute(msg);
                    } catch (SQLException e) {
                        LOGGER.debug("Error in insertion in poller table");
                    }
                });

               // System.out.println(query);
                handler.reply("successful");
            }catch (Exception e){
                LOGGER.info("Exception Occurred :"+e.getMessage());
                handler.reply(e.getMessage());
            }
        });
        startPromise.complete();
    }

}
