package pers.web.rest;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import pers.web.rest.s1.S1Service;
import pers.web.rest.s2.S2Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * <p>在此部署所有的service</p>
 * improve me!
 * @author liang gong
 */
public class ServiceDeployer {
    private static final Logger logger = LoggerFactory.getLogger(ServiceDeployer.class);

    static Future<JsonObject> deploy(Vertx vertx, JsonObject config) {
        Promise<JsonObject> promise = Promise.promise();
        // sample service
        Future<Boolean> s1Future = deployS1(vertx, config);
        Future<Boolean> s2Future = deployS2(vertx, config);
        // 部署顺序无序，如果依赖部署顺序，应使用compose方式
        CompositeFuture.all(Arrays.asList(s1Future, s2Future))
                .onSuccess(compositeFuture -> promise.complete(config))
                .onFailure(compositeFuture -> promise.fail(compositeFuture.getCause()));
        return promise.future();
    }

    static Future<Boolean> deployS1(Vertx vertx, JsonObject config) {
        return Future.future(promise -> {
            DeploymentOptions s1Opts = new DeploymentOptions()
                    .setInstances(1).setWorker(true).setConfig(config);
            vertx.deployVerticle(S1Service::new, s1Opts, stringAsyncResult -> {
                if (stringAsyncResult.succeeded()) {
                    logger.info("Service:{} deploy.....done.", "S1Service");
                    promise.complete();
                } else {
                    promise.fail(stringAsyncResult.cause());
                }
            });
        });
    }

    static Future<Boolean> deployS2(Vertx vertx, JsonObject config) {
        return Future.future(promise -> {
            DeploymentOptions s2Opts = new DeploymentOptions()
                    .setInstances(1).setWorker(true).setConfig(config);
            vertx.deployVerticle(S2Service::new, s2Opts, stringAsyncResult -> {
                if (stringAsyncResult.succeeded()) {
                    logger.info("Service:{} deploy.....done.", "S2Service");
                    promise.complete();
                } else {
                    promise.fail(stringAsyncResult.cause());
                }
            });
        });
    }
}
