package com.chaochaogege.serverjava;

import com.chaochaogege.serverjava.api.Staff;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;

public class Main extends AbstractVerticle {
    private HttpServer server = null;
    private Router router = null;
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(5000);
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new Main());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        this.server = vertx.createHttpServer(new HttpServerOptions().setPort(3030));
        this.router = Router.router(vertx);
        this.routerInit();
        this.server.requestHandler(router);
        this.server.listen();
        startPromise.complete();
    }
    private void routerInit() {
        new Staff(vertx,router);
    }
}
