package com.chaochaogege.serverjava.api;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

@RunWith(VertxUnitRunner.class)
public class StaffTest {
    public static int PORT = 3030;
    Vertx vertx ;
    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
        Async async = context.async();
        vertx.deployVerticle(TestVerticle.class.getName(),context.asyncAssertSuccess(event -> async.complete()));
    }

    @Test
    public void queryTest(TestContext context) {
        Async async = context.async();
        HttpClient client = vertx.createHttpClient();
        client.get(PORT,"127.0.0.1","/staff").handler(httpClientResponse -> {
            httpClientResponse.bodyHandler(body -> {
                HashMap<String,Object> map = (HashMap<String,Object>)Json.decodeValue(body);
                async.complete();
            });
        }).end();
    }
    @After
    public void after(TestContext context) {
        vertx.close();
    }

    public static class TestVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) throws Exception {
            Router router = Router.router(vertx);
            Staff staff = new Staff(vertx,router);
            router.route(HttpMethod.GET,"/staff").handler(staff::query);
            vertx.createHttpServer().requestHandler(router).listen(PORT,"127.0.0.1",asyncResult -> {
                if (asyncResult.succeeded()) {
                    startPromise.complete();
                    return;
                }
                startPromise.fail(asyncResult.cause());
            });
        }
    }
}
