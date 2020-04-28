package com.chaochaogege.ujnbs.common;

import com.chaochaogege.ujnbs.api.OpResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.*;

public class Util {
    public static JsonArray reorderFromJson(ArrayList<String> array, JsonObject object) {
        JsonArray a = new JsonArray();
        array.forEach( s -> {
            a.add(object.getValue(s));
        });
        return a;
    }
    public static boolean isInteger(String s){
        try{
            Integer.parseInt(s);
        }catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    public static JsonArray rowToArray(RowSet<Row> set) {
        Iterator<Row> it = set.iterator();
        JsonArray array = new JsonArray();
        while(it.hasNext()) {
            Row row = it.next();
            JsonObject obj = new JsonObject();
            for (int idx = 0 ; idx < row.size(); ++ idx) {
                obj.put(row.getColumnName(idx),row.getValue(idx));
            }
            array.add(obj);
        }
        return array;
    }

    public static Handler<RoutingContext> isValidIdHandler(Handler<RoutingContext> handler) {
        return (RoutingContext  context) -> {
            String uid = parseIdFromPath(context.request().path());
            if (!isValidID(uid)){
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_ID,"wrong id: " + uid);
                return;
            }
            handler.handle(context);
        };
    }
    public static Handler<RoutingContext> isNotNullBody(Handler<RoutingContext> handler) {
        return (RoutingContext context) -> {
            JsonObject obj = context.getBodyAsJson();
            if (Objects.isNull(obj) || Objects.isNull(obj.getJsonObject("data"))) {
                OpResult.failedDirectlyWithCause(context.response(),OpResult.STATUS_FAILED_WRONG_POST_DATA,"data is invalid");
                return;
            }
            handler.handle(context);
        };
    }

    public static String parseIdFromPath(String path){
        String[] paths = path.split("/");
        String uid = paths[paths.length - 1];
        if(!isValidID(uid)) return "";
        return uid;
    }

    public static boolean isValidID(String uid) {
        String[] u = stringSplit(uid,7,11,15,19);
        String uuid = String.join("-",u);
        try {
            UUID.fromString(uuid);
        }catch (IllegalArgumentException e) {
            return false;
        }
        return true;
    }

    public static String[] stringSplit(String s, int ...idx) {
        Arrays.sort(idx);
        int last = 0;
        String[] temp = new String[idx.length + 1];
        for (int i = 0 ; i < idx.length ; i ++) {
            temp[i] =  s.substring(last, idx[i] + 1);
            last = idx[i] + 1;
        }
        temp[idx.length] = s.substring(last);
        return temp;
    }
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-","");
    }

    public static void main(String[] args) {
        assert isValidID(uuid());
    }
}
