package com.mindarray.nms.main;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

public class MainVerticle extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(MainVerticle.class);
    @Override
    public void start(Promise<Void> startPromise) throws Exception {

        var eventBus = vertx.eventBus();
        var entries = new JsonArray();
        var credentials = new JsonObject();
        Queue<JsonObject> pollingCredential = new LinkedList<>();
        eventBus.consumer("DiscoveryOccurred",handler ->{
            pollingCredential.add((JsonObject) handler.body());
            handler.reply("New Data received for polling");
        });
        eventBus.consumer("Poller", handler ->{
         credentials.put("Data",handler.body());
         AtomicInteger count = new AtomicInteger();
         var data1 = credentials.getJsonObject("Data");

         credentials.getJsonObject("Data").stream().forEach(configuration ->{
             var object = new JsonObject((credentials.getJsonObject("Data").getJsonObject(configuration.getKey())).put("Unique_key",configuration.getKey()).toString());
             entries.add(count.get(),object);
             count.getAndIncrement();
          });
            long timerid = vertx.setPeriodic(9000,handler2->{
                if(!(pollingCredential.isEmpty())){
                    int size = pollingCredential.size();
                    for(int i= 0 ;i< size;i++){
                        var config = pollingCredential.poll();
                        var config1 = new JsonObject();
                        config1.put("Data",config);
                        config1.getJsonObject("Data").stream().forEach(result ->{
                            try{
                                var unique_id= result.getKey();
                                credentials.getJsonObject("Data").put(unique_id,config.getJsonObject(unique_id));
                                entries.add(entries.size(),new JsonObject(config.getJsonObject(unique_id).toString()).put("Unique_key",unique_id));
                            }catch (Exception e){
                                LOGGER.info(e.getMessage());
                            }
                        });


                    }
                }
                for(int i =0 ;i<entries.size();i++){
                    var data = entries.getJsonObject(i);
                    var time = data.getInteger("Scheduled_Time");
                    if(time-1000 <=0){
                        int finalI = i;
                        eventBus.request("Task1",data, taskHandler->{
                            if(taskHandler.succeeded()){
                                entries.set(finalI,(credentials.getJsonObject("Data").getJsonObject(data.getString("Unique_key"))).put("Unique_key",data.getString("Unique_key")));
                            }else{
                                LOGGER.info("Unable to send Data for Polling after Time becomes Zero");
                            }
                        });
                    }else{
                        data.put("Scheduled_Time",time-1000);
                        entries.set(i,data);
                    }
                }

            });
        });
    }
}
