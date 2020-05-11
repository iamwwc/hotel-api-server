package com.chaochaogege.ujnbs;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.vertx.core.Handler;
import io.vertx.core.http.Cookie;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

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
    public AuthProvider(String secret) {
        this.secret = secret;
        this.algorithm = Algorithm.HMAC256(secret);
        this.vertifier = JWT.require(this.algorithm).withIssuer(issuer).build();
    }
    public AuthProvider(Router router) {
        this(DEFAULT_AUTH);
        router.route("/api/login").handler(ctx -> {
            // use default token to sign without verify user info
            ctx.response().addCookie(Cookie.cookie("jwt_token",this.sign()));
        });
    }

    public String sign() {
        return JWT.create().withIssuer(issuer).sign(this.algorithm);
    }
    public boolean verify(String token) {
        try {
            if (token.equals(DEFAULT_AUTH)){
                return true;
            }
            this.vertifier.verify(token);
            return true;
        }catch (JWTVerificationException e) {
            return false;
        }
    }

    @Override
    public void handle(RoutingContext ctx) {
        HttpServerRequest request = ctx.request();
        String cookies = request.headers().get("cookie");
        cookies = "; " + cookies;
        if(request.path().equals("/api/login")){
            ctx.response().addCookie(Cookie.cookie("jwt_token",this.sign()));
            ctx.end();
            return;
        }
        Pattern pattern = Pattern.compile("jwt_token=([^;]+);?");
        Matcher m = pattern.matcher(cookies);
        String token = m.group(0);
        if (!this.verify(token)) {
            ctx.redirect("/login");
            return;
        }
        ctx.next();
    }
}
