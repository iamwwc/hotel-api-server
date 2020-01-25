package com.chaochaogege.ujnbs;

import io.vertx.mysqlclient.MySQLConnectOptions;

/**
 * APIOptions is a wrap of MysqlConnectOptions
 */
public class APIOptions {
    private MySQLConnectOptions options = new MySQLConnectOptions();
    private int listenPort;
    public APIOptions() {
        // default options
        options.setHost("localhost");
        options.setPort(3306);
        options.setUser("root");
        // default listen port
        this.setListenPort(3030);
    }
    public MySQLConnectOptions getSqlOptions() {
        return this.options;
    }
    public APIOptions setHost(String host) {
        options.setHost(host);
        return this;
    }
    public APIOptions setPort(int port) {
        options.setPort(port);
        return this;
    }
    public APIOptions setUser(String user) {
        options.setUser(user);
        return this;
    }
    public APIOptions setDatabase(String db) {
        options.setDatabase(db);
        return this;
    }
    public APIOptions setPassword(String pwd) {
        options.setPassword(pwd);
        return this;
    }

    public APIOptions setListenPort(int port) {
        listenPort = port;
        return this;
    }
    public int getListenPort() {
        return this.listenPort;
    }
}
