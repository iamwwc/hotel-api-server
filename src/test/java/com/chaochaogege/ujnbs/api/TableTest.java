package com.chaochaogege.ujnbs.api;

import com.chaochaogege.ujnbs.APIGenerator;
import com.chaochaogege.ujnbs.APIOptions;
import com.chaochaogege.ujnbs.TableColumn;
import io.vertx.core.*;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientResponse;
import io.vertx.core.http.RequestOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;

//如下测试需存在mysql，否则test fail
@RunWith(VertxUnitRunner.class)
public class TableTest {
    private static Logger logger = LoggerFactory.getLogger(TableTest.class.getName());
    public static int PORT = 3030;
    Vertx vertx;
    HttpClient client;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx(new VertxOptions().setBlockedThreadCheckInterval(Integer.MAX_VALUE));
        client = vertx.createHttpClient();
        APIOptions options = new APIOptions().setHost("localhost")
                .setDatabase("hotel")
                .setPort(3306)
                .setUser("root")
                .setPassword("wxlwuweichao");

        ArrayList<TableColumn> columns = new ArrayList<>();
        TableColumn table1 = new TableColumn("staff", "id", new ArrayList<>(Arrays.asList("username", "id", "role", "email", "phone", "sex")));
        columns.add(table1);
        new APIGenerator(options, columns).run();
    }

    @Test
    public void tableStaffTest(TestContext context) {
        Async async = context.async(99);
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
            Promise<String> p = Promise.promise();
            response.bodyHandler(b -> {
                JsonObject o = b.toJsonObject();
                context.assertTrue(o.containsKey("code") && ((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1));
                String lastId = o.getJsonObject("data").getString("lastId");
                p.complete(lastId);
                async.countDown();
            });
            return p.future();
        }).compose(lastId -> {
            Handler<HttpClientResponse> handler = httpClientResponse -> {
                httpClientResponse.bodyHandler(body -> {
                    JsonObject map = (JsonObject) Json.decodeValue(body);
                    context.assertTrue(map.containsKey("code") && map.containsKey("data"));
                    async.countDown();
                });
            };
            Future<Void> r1 = client.get(PORT, "127.0.0.1", "/staff").setHandler(context.asyncAssertSuccess(handler)).end();
            Future<Void> r2 = client.get(PORT, "localhost", "/staff/").setHandler(context.asyncAssertSuccess(handler)).end();
            Future<Void> r3 = client.get(PORT, "localhost", "/staff/" + lastId).setHandler(context.asyncAssertSuccess(response -> {
                response.bodyHandler(b -> {
                    JsonObject o = b.toJsonObject();
                    context.assertTrue((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1);
                    context.assertTrue(o.getJsonArray("data").getJsonObject(0).getString("username").equals(username));
                });
            })).end();
            return CompositeFuture.all(Arrays.asList(r1, r2, r3)).map(lastId);
        }).compose(uid -> {
            // update test
            // 使用 uid 更新 username
            data.put("username", newUserName);
            Promise<String> p = Promise.promise();
            client.post(3030, "localhost", "/staff/" + uid).setHandler(context.asyncAssertSuccess(o1 -> {
                o1.bodyHandler(b -> {
                    JsonObject o = b.toJsonObject();
                    context.assertTrue((o.getInteger("code") & OpResult.STATUS_SUCCEED) == 1);
                    logger.debug("Update username: {} completed", newUserName);
                    async.countDown();
                    p.complete(uid);
                });
            })).end(Json.encodeToBuffer(postData));
            return p.future();
        }).compose(uid -> {
            Promise<Void> promise = Promise.promise();
            client.delete(3030, "localhost", "/staff/" + uid).setHandler(r -> {
                if (r.succeeded()) {
                    HttpClientResponse response = r.result();
                    response.bodyHandler(v -> promise.complete());
                    return;
                }
                context.fail();
            }).end();
            return promise.future();
        }).compose(o -> {
            Promise<Void> promise = Promise.promise();
            client.get(PORT, "localhost", "/staff").setHandler(asyncResult -> {
                if (!asyncResult.succeeded()) context.fail();
                HttpClientResponse response = asyncResult.result();
                response.bodyHandler(body -> {
                            JsonObject obj = (JsonObject) Json.decodeValue(body);
                            if (obj.getInteger("code") == OpResult.STATUS_SUCCEED) {
                                async.complete();
                                promise.complete();
                                return;
                            }
                            context.fail();
                        }
                );
            }).end();
            return promise.future();
        }).otherwise(v -> {
            context.fail(v);
            return null;
        });
    }

    @After
    public void after(TestContext context) {
        vertx.close();
    }
}
