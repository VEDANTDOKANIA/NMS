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

public class HttpListener extends AbstractVerticle {

    public static final int PORT = 8080;
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpListener.class);
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
           Future<JsonObject> future = Discovery.initialDiscovery(credentials);
           future.onComplete(handler ->{
               if(future.succeeded()){

                   if(future.result().getString("Error").equals("null")){
                       response.write(future.result().getString("Message"));
                       eventBus.request("Discovery",future.result(),reply ->{
                           if(reply.result().body().toString().equals("already")){
                               response.write(" \n Error:  IP already discovered.");
                           }else if(reply.result().body().toString().equals("successful")){
                               response.write("\n Data successfully entered into database. \n Discovery Done Successful");
                           }else{
                               response.write("Database Error :"+ reply.result().body().toString());
                           }
                           response.end();
                       });

                   }else{
                       response.write("Error Occurred  :"+future.result().getString("Error"));
                       response.end();
                   }

               }else{
                   LOGGER.debug("Error Occured");
               }
           });




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
