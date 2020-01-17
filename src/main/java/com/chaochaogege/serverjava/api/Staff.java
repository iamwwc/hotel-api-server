package com.chaochaogege.serverjava.api;

import com.chaochaogege.serverjava.common.Util;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

public class Staff {
    private SQLClient client;
    private Router router;
    public static String tableName = "staffs";
    public static ArrayList<String> tableColumns;
    private static Pattern pattern;
    static {
        pattern = Pattern.compile(String.format("%s/?$",tableName));
        tableColumns = new ArrayList<>(Arrays.asList("username","uid","role","email","phone","sex"));
    }
    public  Staff(Vertx vertx, Router router) {
        this.router = router;
        JsonObject object = new JsonObject();
        object.put("host","127.0.0.1")
                .put("username","root")
                .put("database","hotel")
                .put("password","wxlwuweichao")
                .put("port",3306);

        this.client = MySQLClient.createShared(vertx,object);
        router.route(HttpMethod.GET,String.format("/%s/*",tableName)).handler(this::query);
        router.route(HttpMethod.POST,String.format("/%s/",tableName)).handler(this::insertOrUpdate);
    }
    public void query(RoutingContext ctx) {
        String path = ctx.request().path();
        Promise<ResultSet> promise = Promise.promise();
        promise.future().setHandler(r -> {
           if (r.succeeded()) {
               ResultSet set = r.result();
               JsonArray array = new JsonArray(set.getRows());
               Buffer response = new OpResult(OpResult.STATUS_SUCCEED, array).encode();
               ctx.response().end(response);
           }
        });

        if (pattern.matcher(path).find()) {
            this.client.query("select * from staffs",promise);
            return;
        }
        String[] paths = path.split("/");
        String uid = paths[paths.length - 1];
        if ("".equals(uid)){
            Buffer res = OpResult.failedResponseWith(OpResult.STATUS_FAILED_NO_ENOUGH_ARGS);
            ctx.response().end(res);
            return;
        }
        this.client.queryWithParams(String.format("select * from %s where uid = ?",tableName),new JsonArray().add(uid),promise);
    }
    public void insertOrUpdate(RoutingContext context) {
        JsonObject body = context.getBodyAsJson();
        String sql = String.format("insert into %s(%s) values(?,?,?,?,?,?) on duplicate key update uid=?",tableName,String.join(",",tableColumns));
        JsonObject data = body.getJsonObject("data");
        if (data == null){
            OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_POST_DATA,"no more data found");
            return;
        }
        JsonArray array = Util.reorderFromJson(tableColumns,data);
        client.updateWithParams(sql,array,result -> {
            if(!result.succeeded()){
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,result.cause());
                return;
            }
            OpResult.succeedWithCode(context.response());
        });
    }
    public void delete(RoutingContext context) {
        HttpServerRequest request = context.request();
        String[] p = request.path().split("/");
        String uid = p[p.length - 1];
        if(!Util.isInteger(uid)) {
            OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_UID,"wrong uid found");
            return;
        }
        String sql = "delete from " + tableName + " where uid = ?";
        client.updateWithParams(sql,new JsonArray().add(uid),result -> {
           if (!result.succeeded()) {
               OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,result.cause());
               return;
           }
           OpResult.succeedWithCode(context.response());
        });
    }
}
