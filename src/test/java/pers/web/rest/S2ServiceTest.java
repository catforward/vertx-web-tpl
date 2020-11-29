package pers.web.rest;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * <p>S2Server url测试</p>
 * <p>启动前需要设置${LOG_HOME}环境变量</p>
 * @author liang gong
 */
@RunWith(VertxUnitRunner.class)
public class S2ServiceTest {
    private Vertx vertx;
    private JsonObject wantGet2;
    private final static Object NULL_POINT = null;

    @Before
    public void setUp(TestContext context) {
        vertx = Vertx.vertx();
        wantGet2 = new JsonObject()
                .put("response from","s2handler")
                .put("method", "GET")
                .put("path", "/api/s2/get2")
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
                                .put("db.max_pool_size", 5))
                        .put("from", "request from S1")
                );
    }

    @After
    public void tearDown(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testS1ServiceGet1(TestContext context) {
        final Async async = context.async();

        HttpClientRequest req = vertx.createHttpClient().get(8080, "localhost", "/api/s2/get2");
        req.exceptionHandler(err -> context.fail(err.getMessage()));
        req.handler(resp -> {
            context.assertEquals(200, resp.statusCode());
            context.assertTrue(resp.getHeader("Content-Type").contains("application/json"));
            resp.bodyHandler(body -> {
                System.out.println(wantGet2.encodePrettily());
                System.out.println(body.toString());
                context.assertTrue(wantGet2.equals(body.toJsonObject()));
                async.complete();
            });
        });
        req.end();
    }
}
