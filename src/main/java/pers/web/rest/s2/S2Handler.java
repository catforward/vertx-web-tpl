package pers.web.rest.s2;

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
public class S2Handler implements Handler<RoutingContext> {

    private S2Handler() {}

    public static S2Handler create() {
        // do something others
        return new S2Handler();
    }

    @Override
    public void handle(RoutingContext routingContext) {
        // get input
        JsonObject body = routingContext.getBodyAsJson();
        JsonObject data = new JsonObject();
        data.put("response from", "s2handler");
        data.put("method", routingContext.request().method());
        data.put("path", routingContext.request().path());
        data.put("body", body);
        // write output
        routingContext.vertx().eventBus().request(Symbols.ServiceAddress.S2_SERVICE, body, (AsyncResult<Message<JsonObject>> res) -> {
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
