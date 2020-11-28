/*
 * RestServerVerticle.java
 *
 *  Copyright 2020  liang gong. All rights reserved.
 */
package pers.web.rest;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.ErrorHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Http的Rest服务</p>
 *
 * @author liang gong
 */
public class RestServerVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(RestServerVerticle.class);
    HttpServer server;

    @Override
    public void start() throws Exception {
        super.start();
        server = createHttpServer();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (server != null) {
            server.close();
        }
    }

    private HttpServer createHttpServer() {
        Router router = Router.router(vertx);
        // session
        LocalSessionStore store = LocalSessionStore.create(vertx);
        router.route().handler(SessionHandler.create(store));
        // default handler
        router.route().handler(BodyHandler.create());
        router.route().failureHandler(ErrorHandler.create());
        // root path
        router.route("/api").handler(context -> {
            // 默认行为：返回版本号
            context.response().end(new JsonObject().put("version", "v1").encodePrettily());
        });

        // deploy our rest handler
        RouterDeployer.deploy(vertx, router);

        String host = config().getString("http.address", "localhost");
        int port = config().getInteger("http.port", 8081);

        // TODO enable HTTPS

        // create http server
        return vertx.createHttpServer().requestHandler(router).listen(port, host, ar -> {
            if (ar.succeeded()) {
                logger.info("Rest Server is running on port: {} ", port);
            } else {
                logger.error("Failed to start Rest Server", ar.cause());
            }
        });
    }

}
