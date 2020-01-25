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

    /**
     * 用于内部使用的MysqlConnectOptions
     * @return
     */
    public MySQLConnectOptions getSqlOptions() {
        return this.options;
    }

    /**
     * mysql 数据库地址
     * 默认为 localhost
     * @param host mysql IP 地址
     * @return
     */
    public APIOptions setHost(String host) {
        options.setHost(host);
        return this;
    }

    /**
     * @param port mysql 数据库端口 ，默认 3306
     * @return
     */
    public APIOptions setPort(int port) {
        options.setPort(port);
        return this;
    }

    /**
     *
     * @param user mysql 用户名，默认 root
     * @return
     */
    public APIOptions setUser(String user) {
        options.setUser(user);
        return this;
    }

    /**
     *
     * @param db mysql数据库名
     * @return
     */
    public APIOptions setDatabase(String db) {
        options.setDatabase(db);
        return this;
    }

    /**
     * @param pwd mysql 对应用户名的密码
     * @return
     */
    public APIOptions setPassword(String pwd) {
        options.setPassword(pwd);
        return this;
    }

    /**
     *
     * @param port API 监听端口
     * @return
     */
    public APIOptions setListenPort(int port) {
        listenPort = port;
        return this;
    }

    /**
     *
     * @return API 监听端口
     */
    public int getListenPort() {
        return this.listenPort;
    }
}
