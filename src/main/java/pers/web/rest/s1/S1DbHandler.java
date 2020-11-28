/*
 * S1DbHandler.java, 2020-11-27
 *
 *  Copyright 2020  XXX, Inc. All rights reserved.
 */

package pers.web.rest.s1;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.eventbus.Message;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import pers.web.rest.Symbols;

public class S1DbHandler implements Handler<RoutingContext> {
    private S1DbHandler() {}

    public static S1DbHandler create() {
        // do something others
        return new S1DbHandler();
    }

    @Override
    public void handle(RoutingContext routingContext) {
        // get input
        JsonObject body = routingContext.getBodyAsJson();
        JsonObject data = new JsonObject();
        data.put("response from", "S1DbHandler");
        data.put("method", routingContext.request().method());
        data.put("path:", routingContext.request().path());
        data.put("body", body);

        // write output
        routingContext.vertx().eventBus().request(Symbols.ServiceAddress.S1_SERVICE_DB, body, (AsyncResult<Message<JsonObject>> res) -> {
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
