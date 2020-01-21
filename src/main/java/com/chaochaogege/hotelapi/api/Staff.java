package com.chaochaogege.hotelapi.api;

import com.chaochaogege.hotelapi.common.Util;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.sql.UpdateResult;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Pattern;

public class Staff {
    private SQLClient client;
    private Router router;
    public static String tableName = "staff";
    public static ArrayList<String> tableColumns;
    private static Pattern pattern;
    private static String updateOrInsertSQL;
    private static String primaryKey = "uid";
    static {
        pattern = Pattern.compile(String.format("%s/?$", tableName));
        // username, uid, role, email, phone, sex
        // uid 不应该insert，应由db作为pk生成
        tableColumns = new ArrayList<>(Arrays.asList("username", "role", "email", "phone", "sex"));
        updateOrInsertSQL = String.format("insert into %s(%s) values(?,?,?,?,?) on duplicate key update uid=?", tableName, String.join(",", tableColumns));
    }

    public Staff(Vertx vertx, Router router) {
        this.router = router;
        JsonObject object = new JsonObject();
        object.put("host", "127.0.0.1")
                .put("username", "root")
                .put("database", "hotel")
                .put("password", "wxlwuweichao")
                .put("port", 3306);

        this.client = MySQLClient.createShared(vertx, object);
        router.route(HttpMethod.GET, String.format("/%s/*", tableName)).handler(this::query);
        router.route(HttpMethod.DELETE, String.format("/%s/", tableName)).handler(this::delete);

//        router.route(HttpMethod.POST, String.format("/%s/", tableName)).handler(this::insertOrUpdate);
        router.route().method(HttpMethod.POST).pathRegex(String.format("/%s/?$", tableName)).handler(this::insert);
        router.route().method(HttpMethod.POST).path(String.format("/%s/*", tableName)).handler(this::update);
    }
    public static String updateOrInsertSQL(JsonObject obj, JsonArray array, String tName, String pk) {
        // 应该让phone作为pk
        // uid 作为 unique index
        String sql = "insert into hotel(%s) values(%s) on duplicate key update username";
        String sqlTemplate = "insert into %s(%s) values(%s) on duplicate key update %s=?";
        Iterator<Map.Entry<String,Object>> it = obj.iterator();
        ArrayList<String> keyArray = new ArrayList<>();
        ArrayList<Object> valueArray = new ArrayList<>();
        while(it.hasNext()) {
            Map.Entry<String,Object> entry = it.next();
            String key = entry.getKey();
            Object value = entry.getValue();
            keyArray.add(key);
            valueArray.add(value);
        }
        array.addAll(new JsonArray(valueArray));
        String [] placeHolder = new String[keyArray.size()];
        Arrays.fill(placeHolder,"?");
        return String.format(sqlTemplate,tName, String.join(",",keyArray),String.join(",",placeHolder),pk);
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
            this.client.query("select * from staff", promise);
            return;
        }
        String[] paths = path.split("/");
        String uid = paths[paths.length - 1];
        if ("".equals(uid)) {
            Buffer res = OpResult.failedResponseWith(OpResult.STATUS_FAILED_NO_ENOUGH_ARGS);
            ctx.response().end(res);
            return;
        }
        this.client.queryWithParams(String.format("select * from %s where uid = ?", tableName), new JsonArray().add(uid), promise);
    }

    private void insertOrUpdate(JsonObject obj, Handler<AsyncResult<UpdateResult>> handler) {
        JsonArray array = new JsonArray();
        String sql = updateOrInsertSQL(obj,array,tableName,primaryKey);
        client.updateWithParams(sql, array,handler);
    }

    public void insert(RoutingContext context) {

        JsonObject obj = context.getBodyAsJson().getJsonObject("data");
        JsonArray values = sortValueFromColumn(obj);
        String[] placeHolder = new String[values.size()];
        Arrays.fill(placeHolder,"?");
        String onUpdate =  tableColumns.stream().reduce("",(prev,next) -> String.format("%s %s=?",prev, next)).trim().replace(" ",",");
        String sql  = String.format("insert into %s(%s) values (%s) on duplicate key update %s",tableName, String.join(",",tableColumns),String.join(",",placeHolder),onUpdate);
        client.updateWithParams(sql,values.addAll(values),updateResultAsyncResult -> {
            if (updateResultAsyncResult.succeeded()) {
                UpdateResult result = updateResultAsyncResult.result();
            }else {

            }
        });
    }

    private JsonArray sortValueFromColumn(JsonObject data) {
        JsonArray array = new JsonArray();
        for ( String d : tableColumns) {
            array.add(data.getValue(d));
        }
        return array;
    }

    public void update(RoutingContext context) {
        JsonObject obj = context.getBodyAsJson();
        insertOrUpdate(obj, updateResultAsyncResult -> {
            Buffer body;
            if (updateResultAsyncResult.succeeded()) {
                int key = updateResultAsyncResult.result().getKeys().getInteger(0);
                JsonObject dataObj = new JsonObject().put("data",new JsonObject().put(primaryKey,key));
                body = new OpResult(OpResult.STATUS_SUCCEED,dataObj).encode();
            }else
                body = new OpResult(OpResult.STATUS_FAILED_SQL, new JsonObject().put("data",updateResultAsyncResult.cause())).encode();
            context.response().end(body);

        });
    }

    public void delete(RoutingContext context) {
        HttpServerRequest request = context.request();
        String[] p = request.path().split("/");
        String uid = p[p.length - 1];
        if (!Util.isInteger(uid)) {
            OpResult.failedDirectlyWithCause(context.response(), OpResult.STATUS_FAILED_WRONG_UID, "wrong uid found");
            return;
        }
        String sql = "delete from " + tableName + " where uid = ?";
        client.updateWithParams(sql, new JsonArray().add(uid), result -> {
            if (!result.succeeded()) {
                OpResult.failedDirectlyWithCause(context.response(), OpResult.STATUS_FAILED_SQL, result.cause());
                return;
            }
            OpResult.succeedWithCode(context.response());
        });
    }
}
