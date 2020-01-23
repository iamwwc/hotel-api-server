package com.chaochaogege.hotelapi.api;

import com.chaochaogege.hotelapi.Main;
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
import io.vertx.ext.web.handler.BodyHandler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(VertxUnitRunner.class)
public class TableTest {
    public static int PORT = 3030;
    Vertx vertx;
    HttpClient client;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(5000));
        client = vertx.createHttpClient();
        Async async = context.async();
        vertx.deployVerticle(TestVerticle.class.getName(), context.asyncAssertSuccess(event -> async.complete()));
    }

    @Test
    public void tableStaffTest(TestContext context) {
        Async async = context.async(4);
        String username = "斗宗强者";
        String newUserName = "斗圣强者";
        // insert test
        JsonObject data = new JsonObject()
                .put("username", "斗宗强者")
                .put("phone", "110")
                .put("role", "administrator")
                .put("email", "iam.wuweichao@gmail.com")
                .put("sex", "male");
        JsonObject postData = new JsonObject().put("data", data);
        RequestOptions insertPostOptions = new RequestOptions()
                .setHost("localhost")
                .setPort(3030)
                .setURI("/staff/");
        Promise<HttpClientResponse> orderedHandler = Promise.promise();
        client.post(insertPostOptions).setHandler(orderedHandler).end(Json.encodeToBuffer(postData));
        orderedHandler.future().compose(response -> {
            Promise<Integer> p = Promise.promise();
            response.bodyHandler(b -> {
                JsonObject o = b.toJsonObject();
                context.assertTrue(o.containsKey("code") && ((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1));
                int uid = o.getJsonObject("data").getInteger("uid");
                p.complete(uid);
                async.countDown();
            });
            return p.future();
        }).compose(uid -> {
            // 这里要注意顺序，虽然insert在query之前，但由于是async，并不能保证顺序，需要使用future来控制顺序
            // https://vertx.io/docs/guide-for-java-devs/#_testing_vert_x_code
            // 刚才查了下，future 有 compose，类似js的Promise.resolve().then()方法
            // Future#setHandler 会在 future为 complete 时调用，并不等同于 then()

            // query test
            Handler<HttpClientResponse> handler = httpClientResponse -> {
                httpClientResponse.bodyHandler(body -> {
                    JsonObject map = (JsonObject) Json.decodeValue(body);
                    context.assertTrue(map.containsKey("code") && map.containsKey("data"));
                    async.countDown();
                });
            };
            Future<Void> r1 = client.get(PORT, "127.0.0.1", "/staff").setHandler(context.asyncAssertSuccess(handler)).end();
            Future<Void> r2 = client.get(PORT, "localhost", "/staff/").setHandler(context.asyncAssertSuccess(handler)).end();
            Future<Void> r3 = client.get(PORT, "localhost", "/staff/" + uid).setHandler(context.asyncAssertSuccess(response -> {
                response.handler(b -> {
                    JsonObject o = b.toJsonObject();
                    context.assertTrue((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1);
                    context.assertTrue(o.getJsonArray("data").getJsonObject(0).getString("username").equals(username));
                });
            })).end();
            return CompositeFuture.all(Arrays.asList(r1, r2, r3)).map(uid);
        }).compose(uid -> {
            // update test
            // 使用 uid 更新 username
            data.put("username", newUserName);
            // 这里改成setHandler试一下
            return client.post(3030, "localhost", "/staff/" + uid).end(Json.encodeToBuffer(data)).setHandler(o1 -> async.countDown()).map(uid);
        }).compose(uid -> client.delete(3030, "localhost", "/staff/" + uid).end())
                .setHandler(o -> client.get(PORT, "localhost", "/staff").setHandler(context.asyncAssertSuccess(response -> {
                    response.body().setHandler(body -> {
                        if (body.succeeded()) {
                            JsonObject obj = (JsonObject) Json.decodeValue(body.result());
                            if (obj.getInteger("code") == OpResult.STATUS_SUCCEED) {
                                async.countDown();
                                return;
                            }
                        }
                        context.fail();
                    });
                }))).otherwise(v -> {
                    v.printStackTrace();
                    return null;
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close();
    }

    public static class TestVerticle extends Main {

    }
}
