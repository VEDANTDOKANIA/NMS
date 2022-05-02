package com.mindarray.nms.polling;

import com.mindarray.nms.main.Bootstrap;
import io.netty.handler.codec.base64.Base64Encoder;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class Polling extends AbstractVerticle {
    private static final Logger LOGGER = LoggerFactory.getLogger(Polling.class);
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        EventBus eventBus = vertx.eventBus();
        String address = "Polling" + config().getInteger("subAddress");
        var fs = vertx.fileSystem();
        eventBus.consumer(address,handler ->{
            JsonObject credentials = new JsonObject(handler.body().toString());
            credentials.put("category","Polling");
            String encoder = (Base64.getEncoder().encodeToString((credentials).toString().getBytes(StandardCharsets.UTF_8)));
            try {
                var process = new ProcessBuilder("src/main/java/com/mindarray/nms/main/plugin.exe",encoder).start();
                var processInputStream = process.getInputStream();
                 var reader = new BufferedReader(new InputStreamReader(processInputStream));
                String line ;
                while ((line = reader.readLine()) != null) {
                    JsonObject jsonObject = new JsonObject(line);
                    vertx.fileSystem().open("src/main/resources/Data.txt",new OpenOptions().setAppend(true),result ->{
                        if(result.succeeded()){
                            AsyncFile file = result.result();
                            Buffer buff = Buffer.buffer(String.valueOf(jsonObject));
                            file.write(buff,fileHandler ->{
                                if(fileHandler.succeeded()){
                                    LOGGER.info("Data Return into file");
                                }else{
                                    LOGGER.info("Unable to Write data");
                                }
                            });
                        }
                    });
                    System.out.println(jsonObject);
                }
                handler.reply(address);
            } catch (IOException e) {
               LOGGER.info("Unable to pole the Device:"+ e.getCause().getMessage());
            }
        });
        startPromise.complete();
    }
}
