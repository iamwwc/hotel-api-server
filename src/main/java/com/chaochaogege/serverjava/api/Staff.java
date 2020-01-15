package com.chaochaogege.serverjava.api;

import io.vertx.core.*;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

public class Staff {
    private SQLClient client;
    private Router router;
    public Staff(Vertx vertx, Router router) {
        this.router = router;
        JsonObject object = new JsonObject();
        object.put("host","127.0.0.1")
                .put("username","root")
                .put("database","hotel")
                .put("password","wxlwuweichao")
                .put("port",3306);

        this.client = MySQLClient.createShared(vertx,object);
        router.route(HttpMethod.GET,"/staff").handler(this::query);
    }
    public void query(RoutingContext ctx) {
        String path = ctx.request().path();
        Promise<ResultSet> promise = Promise.promise();
        promise.future().setHandler(r -> {
           ctx.response().end("helloworld");
        });
        if (path.endsWith("staff")) {
            this.client.query("select * from staffs",promise);
            return;
        }
        String[] paths = path.split("/");
        String uid = paths[paths.length - 1];
        this.client.queryWithParams("select * from staffs where uid = ?",new JsonArray().add(uid),promise);
    }
}
