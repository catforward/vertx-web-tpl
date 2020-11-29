package pers.web.rest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>S1Server url测试</p>
 * <p>启动前需要设置${LOG_HOME}环境变量</p>
 * @author liang gong
 */
@RunWith(VertxUnitRunner.class)
public class S1ServiceTest {
    private Vertx vertx;
    private JsonObject wantGet1;
    private JsonObject wantQuery1;
    private final static Object NULL_POINT = null;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        wantGet1 = new JsonObject()
                .put("response from","s1handler")
                .put("method", "GET")
                .put("path", "/api/s1/get1")
                .put("body", NULL_POINT)
                .put("service_result", new JsonObject()
                .put("success", true)
                .put("code", 200)
                .put("message", "successful")
                .put("data", new JsonObject()
                .put("http.address", "0.0.0.0")
                .put("http.port", 8080)
                .put("db.url","jdbc:h2:mem:DBName;DB_CLOSE_DELAY=-1")
                .put("db.driver", "org.h2.Driver")
                .put("db.user","root")
                .put("db.password","root")
                .put("db.max_pool_size", 5)));
        wantQuery1 = new JsonObject()
                .put("response from","S1DbHandler")
                .put("method", "GET")
                .put("path", "/api/s1/query1")
                .put("body", NULL_POINT)
                .put("service_result", new JsonObject()
                        .put("success", true)
                        .put("code", 200)
                        .put("message", "successful")
                        .put("data", new JsonObject()
                        .put("rows", new JsonArray()
                            .add(new JsonObject().put("ID",1).put("NAME","tom"))
                            .add(new JsonObject().put("ID",2).put("NAME","jack"))
                            .add(new JsonObject().put("ID",3).put("NAME","marry")))
                        )
                );

    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testS1ServiceGet1(TestContext context) {
        final Async async = context.async();

        HttpClientRequest req = vertx.createHttpClient().get(8080, "localhost", "/api/s1/get1");
        req.exceptionHandler(err -> context.fail(err.getMessage()));
        req.handler(resp -> {
            context.assertEquals(200, resp.statusCode());
            context.assertTrue(resp.getHeader("Content-Type").contains("application/json"));
            resp.bodyHandler(body -> {
                //System.out.println(wantGet1.encodePrettily());
                //System.out.println(body.toString());
                context.assertTrue(wantGet1.equals(body.toJsonObject()));
                async.complete();
            });
        });
        req.end();
    }

    @Test
    public void testS1ServiceQuery1(TestContext context) {
        final Async async = context.async();

        HttpClientRequest req = vertx.createHttpClient().get(8080, "localhost", "/api/s1/query1");
        req.exceptionHandler(err -> context.fail(err.getMessage()));
        req.handler(resp -> {
            context.assertEquals(200, resp.statusCode());
            context.assertTrue(resp.getHeader("Content-Type").contains("application/json"));
            resp.bodyHandler(body -> {
                context.assertTrue(wantQuery1.equals(body.toJsonObject()));
                async.complete();
            });
        });
        req.end();
    }
}
