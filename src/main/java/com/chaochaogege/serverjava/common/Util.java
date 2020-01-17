package com.chaochaogege.serverjava.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;

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
}
