package com.chaochaogege.hotelapi;

import com.chaochaogege.hotelapi.api.Staff;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

public class Main extends AbstractVerticle {
    private HttpServer server = null;
    private Router router = null;
    private MySQLPool client;
    public static void main(String[] args) {
        VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(5000);
        Vertx vertx = Vertx.vertx(options);
        vertx.deployVerticle(new Main());
    }

    @Override
    public void start(Promise<Void> startPromise) {
        this.server = vertx.createHttpServer(new HttpServerOptions().setPort(3030));
        this.router = Router.router(vertx);
        this.router.route().handler(BodyHandler.create());

        MySQLConnectOptions options = new MySQLConnectOptions()
                .setHost("localhost")
                .setPort(3306)
                .setUser("root")
                .setPassword("wxlwuweichao")
                .setDatabase("hotel");
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        client = MySQLPool.pool(options,poolOptions);
        this.routerInit();
        this.server.requestHandler(router);
        this.server.listen();
        startPromise.complete();
    }
    private void routerInit() {
        new Staff(vertx,router);
    }
}
