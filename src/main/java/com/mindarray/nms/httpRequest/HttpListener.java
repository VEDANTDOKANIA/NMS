package com.mindarray.nms.httpRequest;

import io.netty.handler.codec.http.HttpResponse;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
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
        Future<JsonObject> future = DiscoveryRestApi.attach(router);
        future.onComplete(handler ->{

            if(future.succeeded()){
                eventBus.request("Discovery",future.result(), reply->{
                    LOGGER.debug(reply.result().body().toString());
                });
            }else{
                LOGGER.debug(future.cause().getMessage());
            }
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
