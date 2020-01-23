package com.chaochaogege.hotelapi.api;

import io.vertx.ext.web.Router;
import io.vertx.mysqlclient.MySQLPool;

import java.util.ArrayList;
import java.util.Arrays;

public class Staff {
    public static String tableName = "staff";
    public static String pk = "uid";
    public static ArrayList<String> columns = new ArrayList<>(Arrays.asList("username","uid","role","email","phone","sex"));
    public Staff(MySQLPool client, Router router){
        new Table(client,router,tableName,pk,columns);
    }
}