package com.chaochaogege.hotelapi.api;

import io.vertx.core.Vertx;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.SQLClient;
import io.vertx.ext.web.RoutingContext;

public abstract class AbstractTable {
    public static String tableName;
    public static String primaryKey;
    private SQLClient client;

    public abstract void query(RoutingContext context);
    public abstract void update(RoutingContext context);
    public abstract void insert(RoutingContext context);
    public abstract void delete(RoutingContext context);
    public AbstractTable(SQLClient client) {
        this.client = client;
    }
}
