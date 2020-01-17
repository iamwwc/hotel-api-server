package com.chaochaogege.serverjava.api;

import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.Router;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class StaffTest {
    public static int PORT = 3030;
    Vertx vertx ;
    HttpClient client;
    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(5000));
        client = vertx.createHttpClient();
        Async async = context.async();
        vertx.deployVerticle(TestVerticle.class.getName(),context.asyncAssertSuccess(event -> async.complete()));
    }

    @Test
    public void staffsTest(TestContext context) {
        Async async = context.async(3);
        int uid = 6666;
        String username = "斗宗强者";
        // insert test
        JsonObject data = new JsonObject()
                .put("username","斗宗强者")
                .put("phone","110")
                .put("uid",6666)
                .put("role","administrator")
                .put("email","iam.wuweichao@gmail.com")
                .put("sex","male");
        JsonObject postData = new JsonObject().put("data",data);
        RequestOptions insertPostOptions = new RequestOptions()
                .setHost("localhost")
                .setPort(3030)
                .setURI("/staffs/");
        Promise<HttpClientResponse> orderedHandler = Promise.promise();
        client.post(insertPostOptions).setHandler(orderedHandler).end(Json.encodeToBuffer(postData));
        orderedHandler.future().setHandler(context.asyncAssertSuccess(response -> {
            response.handler(b -> {
                JsonObject o = b.toJsonObject();
                context.assertTrue(o.containsKey("code") && ((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1));
                async.countDown();
            });
        }));


        // 这里要注意顺序，虽然insert在query之前，但由于是async，并不能保证顺序，需要使用future来控制顺序
        // https://vertx.io/docs/guide-for-java-devs/#_testing_vert_x_code
        // query test
        Handler<HttpClientResponse> handler = httpClientResponse -> {
            httpClientResponse.bodyHandler(body -> {
                JsonObject map = (JsonObject) Json.decodeValue(body);
                context.assertTrue(map.containsKey("code") && map.containsKey("data"));
                async.countDown();
            });
        };
        client.get(PORT,"127.0.0.1","/staff").setHandler(context.asyncAssertSuccess(handler)).end();
        client.get(PORT,"localhost","/staff/").setHandler(context.asyncAssertSuccess(handler)).end();
        client.get(PORT,"localhost","/staff/" + uid).setHandler(context.asyncAssertSuccess(response -> {
            response.handler(b -> {
                JsonObject o = b.toJsonObject();
                context.assertTrue((o.getInteger("code") & OpResult.STATUS_SUCCEED )== 1);
                context.assertTrue(o.getJsonArray("data").getJsonObject(0).getString("username").equals(username));
            });
        })).end();
        // update test
    }

    @After
    public void after(TestContext context) {
        vertx.close();
    }

    public static class TestVerticle extends AbstractVerticle {
        @Override
        public void start(Promise<Void> startPromise) {
            Router router = Router.router(vertx);
            new Staff(vertx,router);
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
