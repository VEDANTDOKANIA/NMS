package com.mindarray.nms.httpRequest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class HttpListener extends AbstractVerticle {

    public static final int PORT = 8080;
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpListener.class);
    public static final int WINDOWSPORT = 5985;
    public static final int LINUXPORT = 22;
    public static final int NETWORKPORT = 161;

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        LOGGER.info("HTTP Listener Verticle Deployed");
        EventBus eventBus = vertx.eventBus();
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.route().method(HttpMethod.POST).path("/discovery").blockingHandler(context ->{
            HttpServerResponse response = context.response();
            response.setChunked(true);
            var credentials = context.getBodyAsJson();
            credentials.put("category","Discovery");
            if(!(credentials.containsKey("Port"))){
                if(credentials.containsKey("Metric_Type")) {
                    if (credentials.getString("Metric_Type").equals("windows")) {
                        credentials.put("Port", WINDOWSPORT);
                    } else if (credentials.getString("Metric_Type").equals("linux")) {
                        credentials.put("Port", LINUXPORT);
                    } else if (credentials.getString("Metric_Type").equals("network")) {
                        credentials.put("Port", NETWORKPORT);
                    }
                }
            }
            eventBus.request("check",credentials,handler->{
                if(handler.succeeded()){
                    if(handler.result().body().toString().equals("already")){
                        credentials.put("Check","yes");
                        response.write("IP Already Exists");
                        response.end();
                    }else{
                        Future<JsonObject> future = Discovery.initialDiscovery(credentials);
                        future.onComplete(handler1 -> {
                            if (future.succeeded()) {
                                if (future.result().getString("Verification").equals("Successful") && future.result().getString("Availability").equals("Successful") && future.result().getString("Discovery").equals("Successful")) {
                                    //response.write(future.result().toString());
                                    eventBus.request("Discovery", credentials, reply -> {
                                        if (reply.result().body().toString().equals("already")) {
                                            future.result().put("Database Insertion Error", "IP already exists");
                                        } else if (reply.result().body().toString().equals("successful")) {
                                            future.result().put("Database Insertion", "Successful");
                                        } else {
                                            future.result().put("Database Insertion Error :", reply.result().body().toString());
                                        }
                                        response.write(future.result().toString());
                                        response.end();
                                    });
                                } else {
                                    response.write(future.result().toString());
                                    response.end();
                                }

                            } else {
                                LOGGER.info("Error Occured");
                            }
                        });
                        credentials.put("Check","no");
                        LOGGER.info("Error in Checking Function");
                    }
                }
            });

           /* if(credentials.getString("Check").equals("no")  ) {
                Future<JsonObject> future = Discovery.initialDiscovery(credentials);
                future.onComplete(handler1 -> {
                    if (future.succeeded()) {
                        if (future.result().getString("Verification").equals("Successful") && future.result().getString("Availability").equals("Successful") && future.result().getString("Discovery").equals("Successful")) {
                            //response.write(future.result().toString());
                            eventBus.request("Discovery", credentials, reply -> {
                                if (reply.result().body().toString().equals("already")) {
                                    future.result().put("Database Insertion Error", "IP already exists");
                                } else if (reply.result().body().toString().equals("successful")) {
                                    future.result().put("Database Insertion", "Successful");
                                } else {
                                    future.result().put("Database Insertion Error :", reply.result().body().toString());
                                }
                                response.write(future.result().toString());
                                response.end();
                            });
                        } else {
                            response.write(future.result().toString());
                            response.end();
                        }

                    } else {
                        LOGGER.info("Error Occured");
                    }
                });
            }*/

         /*  Future<JsonObject> future = Discovery.initialDiscovery(credentials);
           future.onComplete(handler ->{
               if(future.succeeded()){
                   if(future.result().getString("Verification").equals("Successful")&& future.result().getString("Availability").equals("Successful") && future.result().getString("Discovery").equals("Successful")){
                       //response.write(future.result().toString());
                       eventBus.request("Discovery",future.result(),reply ->{
                           if(reply.result().body().toString().equals("already")){
                              future.result().put("Database Insertion Error","IP already exists");
                           }else if(reply.result().body().toString().equals("successful")){
                               future.result().put("Database Insertion","Successful");
                           }else{
                               future.result().put("Database Insertion Error :", reply.result().body().toString());
                           }
                           response.write(future.result().toString());
                           response.end();
                       });
                   }else{
                       response.write(future.result().toString());
                       response.end();
                   }

               }else{
                   LOGGER.info("Error Occured");
               }
           });*/

        });


      /*  MetricGroupRestApi.attach(router);
        MonitorRestApi.attach(router);
*/
        vertx.createHttpServer().requestHandler(router).exceptionHandler(exception ->{
            LOGGER.error("Exception Occurred"+":"+exception.getCause().getMessage());

        }).listen(PORT, http ->{
            if(http.succeeded()){
                startPromise.complete();
                LOGGER.info("HTTP server started");
            }else{
                startPromise.fail(http.cause());
                LOGGER.error("HTTP server not started :"+ http.cause());
            }

        });

    }
}
