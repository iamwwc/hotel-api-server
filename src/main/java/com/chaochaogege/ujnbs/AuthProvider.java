package com.chaochaogege.ujnbs;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.chaochaogege.ujnbs.api.OpResult;
import com.chaochaogege.ujnbs.common.Util;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuthProvider implements Handler<RoutingContext> {
    private final JWTVerifier vertifier;
    private String secret;
    private Algorithm algorithm;
    private static String DEFAULT_AUTH = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9" +
            ".eyJpc3MiOiJ1am4tYnMtc2VydmVyIn0" +
            ".nArVsN2xlbwjFEFqVCIwxOK-uBXCoyfwX1Cer2-cvcQ";
    private static String issuer = "ujn-bs-server";
    private MySQLPool client;
    public AuthProvider(String secret) {
        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
        this.vertifier = JWT.require(this.algorithm).withIssuer(issuer).build();
    }

    public AuthProvider(Router router, MySQLPool client) {
        this(DEFAULT_AUTH);
        this.client = client;
    }

    public String sign() {
        return JWT.create().withIssuer(issuer).sign(this.algorithm);
    }

    public boolean verify(String token) {
        try {
            if (token.equals(DEFAULT_AUTH)) {
                return true;
            }
            this.vertifier.verify(token);
            return true;
        } catch (JWTVerificationException e) {
            return false;
        }
    }

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String cookies = request.headers().get("cookie");
        if (request.path().equals("/api/login")) {
            // ?username=iamwwc&password=mypassword
            String u = request.getParam("username");
            String p = request.getParam("password");
            this.verifyUser(ctx, u,p);
            return;
        }
        if (cookies == null) {
            ctx.redirect("/login");
            // ctx.next()调用之后会往下继续处理，如果response.end被调用，那么不调用next则会直接返回
            return;
        }
        cookies = "; " + cookies;

        Pattern pattern = Pattern.compile("jwt_token=([^;]+);?");
        Matcher m = pattern.matcher(cookies);
        if (m.find()) {
            String token = m.group(1);
            if (!this.verify(token)) {
                ctx.redirect("/login");
                return;
            }
            ctx.next();
            return;
        }
        ctx.redirect("/login");
    }
    private void verifyUser(RoutingContext ctx, String username, String password) {
        this.client.preparedQuery("select username, password from users where username=?", Tuple.of(username),asyncResult -> {
            if(asyncResult.succeeded()){
                RowSet<Row> rows = asyncResult.result();
                JsonArray array = Util.rowToArray(rows);
                if(array.size() > 0) {
                    for(Object o: array) {
                        JsonObject m = (JsonObject)o;
                        String p = m.getString("password");
                        if (!"".equals(password) && password.equals(p)) {
                            ctx.response().addCookie(Cookie.cookie("jwt_token", this.sign()));
                            ctx.end();
                            return;
                        }
                    }
                }
                OpResult.failedDirectlyWithCause(ctx.response(),OpResult.STATUS_FAILED,"no user");
            }
        });
    }
}
