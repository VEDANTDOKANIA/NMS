package com.mindarray.nms.polling;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class Task extends AbstractVerticle {
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        Queue<String> address = new LinkedList<>();
        address.add("Polling0");
        address.add("Polling1");
        address.add("Polling2");
        address.add("Polling3");
        address.add("Polling4");
        var eventBus = vertx.eventBus();
        Queue<JsonObject> task =new LinkedList<JsonObject>();
        eventBus.consumer("Task1",handler->{
            task.add((JsonObject) handler.body());
           // System.out.println("Tasks:" +task);
            handler.reply("Done");
        });
        vertx.setPeriodic(1000,periodichandler->{
            for(int i =0 ;i<5 ;i++){
                var config = task.poll();
                //System.out.println(config);
                var sendAddress = address.poll();

                if(config == null || sendAddress ==null){
                    if(sendAddress != null){
                        address.add(sendAddress);
                    }else if(config != null){
                        task.add(config);
                    }
                }else{
                    eventBus.request(sendAddress,config,handler ->{
                        if(handler.succeeded()){
                            address.add(handler.result().body().toString());
                        }
                    });
                }
            }

        });
           /* if(config!= null){
                task.forEach(handler ->{});
                var send = address.poll();
                if(address.poll()!=null){
                    eventBus.request(send,config,handler->{
                        if(handler.succeeded()){
                            address.add(handler.result().body().toString());
                        }
                    });
                }else{
                    task.add(config);
                }
        }});*/
        startPromise.complete();

        /*while(true){

            }
            startPromise.complete();
        }*/




    }
}
