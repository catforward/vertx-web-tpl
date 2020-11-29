package pers.web.rest.s1;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pers.web.rest.Symbols;

/**
 * <p>http请求处理</p>
 * @author liang gong
 */
public class S1Handler implements Handler<RoutingContext> {

    private S1Handler() {}

    public static S1Handler create() {
        // do something others
        return new S1Handler();
    }

    @Override
    public void handle(RoutingContext routingContext) {
        // get input
        JsonObject body = routingContext.getBodyAsJson();
        JsonObject data = new JsonObject();
        data.put("response from", "s1handler");
        data.put("method", routingContext.request().method());
        data.put("path", routingContext.request().path());
        data.put("body", body);

        // write output
        routingContext.vertx().eventBus().request(Symbols.ServiceAddress.S1_SERVICE, body, (AsyncResult<Message<JsonObject>> res) -> {
            if (res.failed()) {
                routingContext.fail(res.cause());
            } else {
                HttpServerResponse response = routingContext.response();
                response.putHeader("content-type", "application/json");
                data.put("service_result", res.result().body());
                response.end(data.encodePrettily());
            }
        });
    }
}
