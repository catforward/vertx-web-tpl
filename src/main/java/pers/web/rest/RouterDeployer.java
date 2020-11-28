package pers.web.rest;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import pers.web.rest.s1.S1DbHandler;
import pers.web.rest.s1.S1Handler;
import pers.web.rest.s2.S2Handler;

/**
 * <p>rest的url和handler映射在此定义</p>
 * improve me!
 * @author liang gong
 */
public class RouterDeployer {

    static void deploy(Vertx vertx, Router mainRouter) {
        // sample router
        deployS1(vertx, mainRouter);
        deployS2(vertx, mainRouter);
        // add more
    }

    static void deployS1(Vertx vertx, Router mainRouter) {
        Router s1Router = Router.router(vertx);
        s1Router.get("/get1").handler(S1Handler.create());
        s1Router.get("/query1").handler(S1DbHandler.create());
        mainRouter.mountSubRouter("/api/s1", s1Router);
    }

    static void deployS2(Vertx vertx, Router mainRouter) {
        Router s2Router = Router.router(vertx);
        s2Router.get("/get2").handler(S2Handler.create());
        mainRouter.mountSubRouter("/api/s2", s2Router);
    }

}
