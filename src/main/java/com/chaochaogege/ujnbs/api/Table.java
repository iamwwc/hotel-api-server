package com.chaochaogege.ujnbs.api;

import com.chaochaogege.ujnbs.common.Util;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.ArrayList;
import java.util.Arrays;

import static com.chaochaogege.ujnbs.common.Util.*;

public class Table {
    public String tableName;
    public String primaryKey;
    private MySQLPool client;
    private Router router;
    private ArrayList<String> columns;
    private ArrayList<String> columnsWithoutPK;
    public String QUERY_ALL_SQL;
    public String QUERY_RECORD_SQL;
    public String UPDATE_RECORD_SQL;
    public String INSERT_RECORD_SQL;
    public String DELETE_RECORD_SQL;
    public String DELETE_ALL_SQL;
    public Table(MySQLPool client, Router router, String tableName, String primaryKey, ArrayList<String> columns) {
        this.client = client;
        this.router = router;
        this.tableName = tableName;
        this.primaryKey = primaryKey;
        this.columns = columns;
        this.columnsWithoutPK = new ArrayList<>();
        int idx = columns.indexOf(primaryKey);
        columnsWithoutPK.addAll(columns);
        columnsWithoutPK.remove(idx);

        // router init
        router.routeWithRegex(HttpMethod.GET,String.format("/%s(?:/?|/\\d*)$",tableName)).handler(this::query);
        router.routeWithRegex(HttpMethod.POST,String.format("/%s/?$",tableName)).handler(isNotNullBody(this::insert));
        router.routeWithRegex(HttpMethod.POST,String.format("/%s/\\d+$",tableName)).handler(isValidIdHandler(isNotNullBody(this::update)));
        router.routeWithRegex(HttpMethod.DELETE,String.format("/%s(?:/?|/\\d*)$",tableName)).handler(this::delete);
        sqlGenerator();
    }

    private void sqlGenerator() {
        QUERY_ALL_SQL = String.format("select * from %s",this.tableName);
        QUERY_RECORD_SQL = String.format("select * from %s where %s = ?",this.tableName,this.primaryKey);
        UPDATE_RECORD_SQL = String.format("insert into %s(%s) values()",tableName,String.join(",",columnsWithoutPK));
    }

    // 插入不需要传 id
    // 更新需要 id，也就是pk
    private String updateSqlGenerator(JsonObject obj, ArrayList<Object> array) {
        String set = columnsWithoutPK.stream().reduce("",(prev,next) -> {
            Object v = obj.getValue(next);
            array.add(v);
            return String.format("%s %s=?",prev,next);
        }).trim().replace(" ",",");
        return set;
    }

    private String insertSqlGenerator(JsonObject obj, ArrayList<Object> arrayList) throws ApiException {
        String baseSql = "insert into %s(%s) values(%s)";
        int length = columnsWithoutPK.size();
        for(int idx = 0 ; idx < length; ++idx) {
            Object v = obj.getValue(columnsWithoutPK.get(idx));
            if (v == null) {
                throw new ApiException(String.format("Not enough arguments, %d needed, but %d found",length,idx + 1));
            }
            arrayList.add(v);
        }
        String[] placeHolders = new String[columnsWithoutPK.size()];
        Arrays.fill(placeHolders,"?");
        return String.format(baseSql,tableName,String.join(",",columnsWithoutPK),String.join(",",placeHolders));
    }

    public void query(RoutingContext context) {
        HttpServerRequest request = context.request();
        String path = request.path();
        if (path.endsWith("/" + tableName) || path.endsWith(tableName + "/")) {
            client.query(QUERY_ALL_SQL,asyncResult -> {
                OpResult result ;
                if (asyncResult.succeeded()) {
                    RowSet set = asyncResult.result();
                    JsonArray array = Util.rowToArray(set);
                    result = new OpResult(OpResult.STATUS_SUCCEED,array);
                }else {
                    result = new OpResult(OpResult.STATUS_FAILED_SQL | OpResult.STATUS_FAILED,asyncResult.cause());
                }
                context.end(result.encode());
            });
            return;
        }
        String uid = Util.parseIdFromPath(path);
        if (!Util.isValidID(uid)) {
            OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_ID,"wrong uid");
            return;
        }
        client.preparedQuery(QUERY_RECORD_SQL, Tuple.wrap(uid), asyncResult -> {
            if (!asyncResult.succeeded()) {
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,asyncResult.cause());
                return;
            }
            RowSet<Row> rows = asyncResult.result();
            JsonArray array = Util.rowToArray(rows);
            context.response().end(new OpResult(OpResult.STATUS_SUCCEED,array).encode());
        });
    }

    public void update(RoutingContext context){
        String baseUpdate = "update %s set %s where %s=?";
        JsonObject obj = context.getBodyAsJson().getJsonObject("data");
        ArrayList<Object> array = new ArrayList<>();
        String sql = updateSqlGenerator(obj,array);
        String id = parseIdFromPath(context.request().path());
        array.add(id);
        String execSql = String.format(baseUpdate,tableName,sql,primaryKey);
        client.preparedQuery(execSql, Tuple.wrap(array.toArray()),sqlResultAsyncResult -> {
            if(!sqlResultAsyncResult.succeeded()) {
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL, sqlResultAsyncResult.cause());
            }
            OpResult.succeedWithCode(context.response());
        });
    }

    public  void insert(RoutingContext context){
        JsonObject obj = context.getBodyAsJson();
        JsonObject data = obj.getJsonObject("data");
        // 即使有pk，也要删除
        // insert 会忽略掉 pk
        obj.remove(primaryKey);
        ArrayList<Object> arrayList = new ArrayList<>();
        String sql = null;
        try {
            sql = insertSqlGenerator(data,arrayList);
        } catch (ApiException e) {
            OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_POST_DATA,e.toString());
            return;
        }
        client.preparedQuery(sql,Tuple.wrap(arrayList.toArray()),rowSetAsyncResult -> {
            if (!rowSetAsyncResult.succeeded()){
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,rowSetAsyncResult.cause());
                return;
            }
            RowSet<Row> rows = rowSetAsyncResult.result();
            long lastId = rows.property(MySQLClient.LAST_INSERTED_ID);
            context.response().end(new OpResult(OpResult.STATUS_SUCCEED,new JsonObject().put("lastId",lastId)).encode());
        });
    }

    public void delete(RoutingContext context){
        String path = context.request().path();
        String sql;
        if (path.endsWith("/"+tableName) || path.endsWith(tableName + "/")) {
            sql = "delete from " + tableName;
            client.preparedQuery(sql,rowSetAsyncResult -> {
                if (!rowSetAsyncResult.succeeded()) {
                    OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,rowSetAsyncResult.cause());
                    return;
                }
                OpResult.succeedWithCode(context.response());
            });
            return;
        }
        String id = Util.parseIdFromPath(path);
        if (!Util.isValidID(id)){
            OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_ID,"invalid id");
            return;
        }
        sql = String.format("delete from %s where %s=?",tableName,primaryKey);
        client.preparedQuery(sql,Tuple.wrap(id),rowSetAsyncResult -> {
            if (!rowSetAsyncResult.succeeded()) {
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_SQL,rowSetAsyncResult.cause());
                return;
            }
            OpResult.succeedWithCode(context.response());
        });
    }
}
