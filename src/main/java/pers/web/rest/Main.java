package pers.web.rest;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *     本程序是个人用来学习vert.x时的模板代码
 *     包含基本的使用方式，但是有大量需要改进的地方
 *     简单描述数据流流向:
 *      http请求：browser
 *               -> http server(vertical)
 *               -> handler
 *               -> eventbus
 *               -> service(vertical) -> domain process
 *      http响应：请求的反方向
 *     基本组件：
 *      handler: 接受HTTP请求，通过eventbus向service发送请求
 *               异步的收到server的响应后，向客户端返回http响应
 *      service: 接受handler发来的请求，处理后返回处理结果
 *     特性及角色
 *      handler: 运行在eventbus的线程上，所以绝对不能在其中编写会阻塞eventbus线程的处理，
 *               否则会影响在event-loop上的其他线程
 *               所以适合放一下接受请求，简单验证后异步的向后端server转发请求的轻操作
 *      service：运行在worker-pool线程上，当有请求来时，有单独的线程来执行service的处理，
 *               不会影响其他线程
 *               所以适合一些io密集，cpu密集型的阻塞处理
 *      约束：
 *       1. 禁止使用反射
 *         原因1：代代JDK对反射优化后的性能依旧无法与直接调用相提并论
 *         原因2：很大可能性会导致项目后期（时间累积或人员变更等）进行功能修改，新增时，
 *               无法百分之百确认对现有代码及机能的影响
 *               （因检索方法，开发者记忆等等原因会漏掉通过反射调用的功能）
 *               而造成上线后的生产故障
 *         原因3：赶个时髦，如果将来想利用graalvm的aot功能编译至native image的话，
 *               就不要使用反射
 *               否则预计会在编译阶段进行痛苦的调试，及编写大量的提示给编译器的反射配置
 *       2. handler中只有参数检查的代码，真实处理使用eventbus向server转发
 *         原因：不要阻塞event-loop线程
 *       3. server只可定义为worker型的vertical
 *         原因：io密集，cpu密集型处理都在worker-pool中执行，不要阻塞其他线程
 * </pre>
 *
 * @author liang gong
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static Vertx vertx;

    public static void main(String[] args) {
        try {
            vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(10));
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                if (vertx != null) { vertx.close(); }
                try {
                    // do something here
                } catch (Exception ex) {
                    logger.error("unknown error occurred!", ex);
                }
            }));

            getConfig().compose(config -> ServiceDeployer.deploy(vertx, config)).onSuccess(config -> {
                logger.info("All Service deploy done.");
                vertx.deployVerticle(RestServerVerticle::new, new DeploymentOptions().setConfig(config));
            }).onFailure(ex -> {
                logger.error("Service deploy failed...", ex.getCause());
                System.exit(1);
            });

        } catch (Exception ex) {
            logger.error("application error occurred!", ex);
            System.exit(1);
        }
    }

    private static Future<JsonObject> getConfig() {
        ConfigRetriever retriever = ConfigRetriever.create(vertx, new ConfigRetrieverOptions().addStore(
            new ConfigStoreOptions().setType("file").setFormat("json").setConfig(new JsonObject().put("path", "config.json"))
        ));
        Promise<JsonObject> promise = Promise.promise();
        retriever.getConfig(ar -> {
            if (ar.succeeded()) {
                promise.complete(ar.result());
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

}
