package com.chaochaogege.ujnbs;

import io.vertx.mysqlclient.MySQLConnectOptions;

/**
 * APIOptions is a wrap of MysqlConnectOptions
 */
public class APIOptions {
    private MySQLConnectOptions options = new MySQLConnectOptions();
    private int listenPort;
    private boolean allowCORS = true;
    private String db;
    private String secret;
    private boolean mustLogin = false;

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
     * @return MysqlConnectOptions
     */
    public MySQLConnectOptions getSqlOptions() {
        return this.options;
    }

    /**
     * mysql 数据库地址
     * 默认为 localhost
     * @param host mysql IP 地址
     * @return APIOptions(this)
     */
    public APIOptions setHost(String host) {
        options.setHost(host);
        return this;
    }

    /**
     * @param port mysql 数据库端口 ，默认 3306
     * @return APIOptions(this)
     */
    public APIOptions setPort(int port) {
        options.setPort(port);
        return this;
    }

    /**
     *
     * @param user mysql 用户名，默认 root
     * @return APIOptions(this)
     */
    public APIOptions setUser(String user) {
        options.setUser(user);
        return this;
    }

    /**
     *
     * @param db mysql数据库名
     * @return APIOptions(this)
     */
    public APIOptions setDatabase(String db) {
        this.db = db;
        options.setDatabase(db);
        return this;
    }

    /**
     * 数据库名
     * @return database name
     */
    public String getDataBaseName() {
        return this.db;
    }

    /**
     * @param pwd mysql 对应用户名的密码
     * @return APIOptions(this)
     */
    public APIOptions setPassword(String pwd) {
        options.setPassword(pwd);
        return this;
    }

    /**
     *
     * @param port API 监听端口
     * @return APIOptions(this)
     */
    public APIOptions setListenPort(int port) {
        listenPort = port;
        return this;
    }

    /**
     * 是否允许CORS，默认允许
     * 当前，CORS只允许GET，POST，DELETE，且并不允许 Credentials
     * @param allowed 允许
     */
    public void allowCORS(boolean allowed) {
        this.allowCORS = allowed;
    }

    /**
     *
     * @return 是否允许CORS
     */
    public boolean isAllowCORS() {
        return this.allowCORS;
    }

    /**
     *
     * @return API 监听端口
     */
    public int getListenPort() {
        return this.listenPort;
    }
    private void secretToken(String token ) {
        this.secret = token;
    }
    public String secret() {
        return this.secret;
    }

    public boolean mustLogin() {
        return this.mustLogin;
    }
}
