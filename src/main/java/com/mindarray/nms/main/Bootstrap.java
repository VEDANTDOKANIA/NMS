package com.mindarray.nms.main;

import com.mindarray.nms.database.Database;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class Bootstrap extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);
    public static void main(String[] args) {
        var vertx = Vertx.vertx();
        for(int i =0 ;i<5 ;i++){
            int finalI = i;
            vertx.deployVerticle("com.mindarray.nms.polling.Polling",new DeploymentOptions().setConfig(new JsonObject().put("subAddress",i)).setWorker(true), handler ->{
                if(handler.succeeded()){
                    LOGGER.info("Polling Verticle"+ finalI +" Deployed");
                }
            });
        }



       vertx.deployVerticle("com.mindarray.nms.polling.Task",new DeploymentOptions(),handler->{
           if(handler.succeeded()){
               LOGGER.info("Task Verticle Deployed");
           }else{
               LOGGER.error("Unable to Deploy Task Verticle");
           }
       });
        vertx.deployVerticle("com.mindarray.nms.main.MainVerticle",new DeploymentOptions()).onComplete(handler ->{
            if(handler.succeeded()){
                LOGGER.info("Main Verticle Deployed");
            }
        });
        vertx.deployVerticle("com.mindarray.nms.database.Initial",new DeploymentOptions().setWorker(true),handler->{
            if(handler.succeeded()){
                LOGGER.info("Initial Verticle Deployed Successfully");
            }else{
                LOGGER.info("Unable to deploy Initial Verticle");
            }
        });
        vertx.deployVerticle("com.mindarray.nms.database.Database",new DeploymentOptions().setWorker(true),handler ->{
            if(handler.succeeded()){
                LOGGER.info("Database Verticle Deployed");
            }else{
                LOGGER.info("Unable to deploy database verticle");
            }
        }
        );
        vertx.deployVerticle("com.mindarray.nms.httpRequest.HttpListener", handler ->{
            if(handler.succeeded()){
                LOGGER.info("Http Listener Verticle Deployed");
            }else{
                LOGGER.info("Unable to Deploy Http Listener Verticle");
            }
        });








    }
}
