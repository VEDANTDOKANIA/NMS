package com.mindarray.nms.main;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Vertx;

public class Bootstrap extends AbstractVerticle {
    public static void main(String[] args) {
        var vertx = Vertx.vertx();
        vertx.deployVerticle("com.mindarray.nms.httpRequest.HttpListener");
    }
}
