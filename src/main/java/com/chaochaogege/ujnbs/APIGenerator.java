package com.chaochaogege.ujnbs;

import com.chaochaogege.ujnbs.api.Table;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.PoolOptions;

import java.io.File;
import java.util.ArrayList;

/**
 * APIGenerator is a wrap of generator
 */
public class APIGenerator {
    private generator api;

    /**
     *
     * @param apiOptions 你初始化的APIOptions 对象
     * @param columns 包含全部需要生成CRUD API的表的集合
     */
    public APIGenerator(APIOptions apiOptions, ArrayList<TableColumn> columns) {
        this.api = new generator(apiOptions, columns);
    }

    public void run() {
        this.api.run();
    }

    /**
     * 获取 Vertx 实例
     * @return vertx 实例
     */
    public Vertx getVertx() {
        return this.api.vertx;
    }

    public static void main(String[] args) {
        // Do nothing
        // used as main entry point in maven-assembly-plugin
    }
}

class generator extends AbstractVerticle {
    private HttpServer server;
    private Router router;
    private MySQLPool client;
    public Vertx vertx;

    public generator(APIOptions apiOptions, ArrayList<TableColumn> columns) {
        System.setProperty("log4j.configuration", new File("." + File.separator + "log4j.properties").toString());
        VertxOptions options = new VertxOptions().setBlockedThreadCheckInterval(5000);
        this.vertx = Vertx.vertx(options);
        this.server = vertx.createHttpServer(new HttpServerOptions().setPort(apiOptions.getListenPort()));
        this.router = Router.router(vertx);
        this.router.route().handler(BodyHandler.create());
        PoolOptions poolOptions = new PoolOptions().setMaxSize(5);
        client = MySQLPool.pool(vertx, apiOptions.getSqlOptions(), poolOptions);
        for (int idx = 0; idx < columns.size(); ++idx) {
            TableColumn t = columns.get(idx);
            new Table(client, router, t.tableName, t.primaryKey, t.columns);
        }
    }

    public generator() {
    }

    public void run() {
        vertx.deployVerticle(this);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        this.server.requestHandler(router);
        this.server.listen();
        startPromise.complete();
    }
}