package com.mindarray.nms.httpRequest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpListener extends AbstractVerticle {

    public static final int PORT = 8080;
    private static final Logger log = LoggerFactory.getLogger(HttpListener.class);
    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        log.info("HTTP Listener Verticle Deployed");
        EventBus eventBus = vertx.eventBus();
        final Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        DiscoveryRestApi.attach(router);
        MetricGroupRestApi.attach(router);
        MonitorRestApi.attach(router);

        vertx.createHttpServer().requestHandler(router).exceptionHandler(exception ->{
            log.error("Exception Occurred"+":"+exception.getCause().getMessage());

        }).listen(PORT, http ->{
            if(http.succeeded()){
                startPromise.complete();
                log.info("HTTP server started");
            }else{
                startPromise.fail(http.cause());
                log.error("HTTP server not started :"+ http.cause());
            }

        });
    }
}
