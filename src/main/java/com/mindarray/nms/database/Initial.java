package com.mindarray.nms.database;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;

public class Initial extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Initial.class);
    public static final String SELECT = "Select column from table where condition";
    public static final String INSERT = "insert into table columns data ;";
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        var conn = DatabaseCommand.connect();
        var stmt = conn.createStatement();
        String databaseCheck = "show databases like" + " \"nms\";" ;
        try {
            ResultSet resultSet = stmt.executeQuery(databaseCheck);
            String value = null;
            while(resultSet.next()){
                value = resultSet.getString(1);
            }
            if(value.equals("nms")){
                LOGGER.info("Database Already Exists");
            }else{
                String createDatabase = "create database nms;" ;
                String createMonitor ="create table monitor (IP_address varchar(50), Metric_type varchar(90), username varchar(90), password varchar(90), port int , community varchar(90) , version varchar(90),Primary key (IP_address) );";
                String createMetric ="create table metric (Metric_type varchar(90), Metric_group varchar(90), time int);";
                String createPoll ="create table poller( Credential_id varchar(100),IP_address varchar(50),Metric_type varchar(90),Metric_group varchar(90),Scheduled_time int, Group_status varchar(20), primary key(Credential_id));";
              var flag= stmt.execute(createDatabase);
              if(flag){
                  LOGGER.info("Database created successfully");
                  stmt.executeQuery("use nms");
                  stmt.executeQuery(createMonitor);
                  stmt.executeQuery(createMetric);
                  stmt.executeQuery(createPoll);
                  stmt.executeQuery("insert into metric values(\"linux\",\"CPU\",5000);");
                  stmt.executeQuery("insert into metric values(\"linux\",\"Disk\",6000);");
                  stmt.executeQuery("insert into metric values(\"linux\",\"Memory\",8000);");
                  stmt.executeQuery("insert into metric values(\"linux\",\"Process\",5000);");
                  stmt.executeQuery("insert into metric values(\"linux\",\"System\",9000);");
                  stmt.executeQuery("insert into metric values(\"windows\",\"CPU\",5000);");
                  stmt.executeQuery("insert into metric values(\"windows\",\"Disk\",6000);");
                  stmt.executeQuery("insert into metric values(\"windows\",\"Memory\",8000);");
                  stmt.executeQuery("insert into metric values(\"windows\",\"Process\",5000);");
                  stmt.executeQuery("insert into metric values(\"windows\",\"System\",9000);");


              }else{
                  LOGGER.info("Unable to create database");
              }
            }
        }catch (Exception e)
        {
            LOGGER.error("Error:" + e.getCause().getMessage());
        }
        startPromise.complete();
    }
}
