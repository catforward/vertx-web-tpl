package pers.web.rest.s1;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import pers.web.rest.common.ResultEnum;
import pers.web.rest.common.ResultMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.web.rest.Symbols;

import java.util.Arrays;
import java.util.List;

public class S1Service extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(S1Service.class);
    private JDBCClient jdbcClient;

    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(Symbols.ServiceAddress.S1_SERVICE, this::handleS1Service);
        bus.consumer(Symbols.ServiceAddress.S1_SERVICE_DB, this::handleS1ServiceDb);
        // sample code
        JsonObject config = new JsonObject();
        config.put("provider_class", "io.vertx.ext.jdbc.spi.impl.HikariCPDataSourceProvider");
        config.put("jdbcUrl", config().getString("db.url"));
        config.put("username", config().getString("db.user"));
        config.put("password", config().getString("db.password"));
        config.put("driverClassName", config().getString("db.driver"));
        config.put("maximumPoolSize", config().getInteger("db.max_pool_size"));
        jdbcClient = JDBCClient.create(vertx, config);
        initMemoryDb();

        logger.info("S1Service-> deploymentId::{}", deploymentID());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        if (jdbcClient != null) { jdbcClient.close(); }
    }

    private void handleS1Service(Message<JsonObject> msg) {
        JsonObject resultBody = new JsonObject();
        resultBody.put("config", config());

        ResultMsg response = new ResultMsg(ResultEnum.SUCCESS);
        response.setData(resultBody);

        msg.reply(response.toJsonObject());
    }

    private void handleS1ServiceDb(Message<JsonObject> msg) {
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection conn = ar.result();
                begin(conn)
                .compose(this::getJsonArrayRows)
                .onSuccess(array -> {
                    conn.commit(arc -> {
                        if (arc.succeeded()) {
                            logger.debug("init memory database commit ok");
                        }
                    }).close();
                    ResultMsg response = new ResultMsg(ResultEnum.SUCCESS);
                    response.setArrayData("rows", array);
                    msg.reply(response.toJsonObject());
                }).onFailure(ex -> {
                    conn.rollback(arc -> {
                        if (arc.succeeded()) {
                            logger.debug("init memory database rollback ok");
                        }
                    }).close();
                });
            } else {
                logger.error("query data error...", ar.cause());
                ResultMsg response = new ResultMsg(ResultEnum.SERVER_ERROR);
                response.setData(new JsonObject().put("cause", ar.cause().getMessage()));
                msg.reply(response.toJsonObject());
            }
        });
    }

    private Future<SQLConnection> begin(SQLConnection conn) {
        Promise<SQLConnection> promise = Promise.promise();
        conn.setAutoCommit(false, ar -> {
            if (ar.succeeded()) {
                promise.complete(conn);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }

    private void initMemoryDb() {
        jdbcClient.getConnection(ar -> {
            if (ar.succeeded()) {
                SQLConnection conn = ar.result();
                begin(conn)
                .compose(this::createTable)
                .compose(this::insertDummyData)
                .onSuccess(re -> {
                    conn.commit(arc -> {
                        if (arc.succeeded()) {
                            logger.debug("init memory database commit ok");
                        }
                    }).close();
                }).onFailure(ex -> {
                    conn.rollback(arc -> {
                        if (arc.succeeded()) {
                            logger.debug("init memory database rollback ok");
                        }
                    }).close();
                });
            } else {
                logger.error("init memory database error...", ar.cause());
            }
        });
    }

    private Future<SQLConnection> createTable(SQLConnection conn) {
        Promise<SQLConnection> promise = Promise.promise();
        conn.execute("CREATE TABLE USER_INF(id INTEGER PRIMARY KEY, name VARCHAR(100))", ret -> {
            if (ret.succeeded()) {
                logger.info("create table success.");
                promise.complete(conn);
            } else {
                promise.fail(ret.cause());
            }
        });
        return promise.future();
    }

    private Future<SQLConnection> insertDummyData(SQLConnection conn) {
        Promise<SQLConnection> promise = Promise.promise();
        List<String> sqls = Arrays.asList("INSERT INTO USER_INF VALUES(1, 'tom') "
                ,"INSERT INTO USER_INF VALUES(2, 'jack') "
                ,"INSERT INTO USER_INF VALUES(3, 'marry') ");
        conn.batch(sqls, ret -> {
            if (ret.succeeded()) {
                logger.info("insert data success.");
                promise.complete(conn);
            } else {
                promise.fail(ret.cause());
            }
        });
        return promise.future();
    }

    private Future<JsonArray> getJsonArrayRows(SQLConnection conn) {
        Promise<JsonArray> promise = Promise.promise();
        conn.query("select id, name from USER_INF", ar -> {
            if (ar.succeeded()) {
                JsonArray array = new JsonArray();
                ar.result().getRows().forEach(array::add);
                promise.complete(array);
            } else {
                promise.fail(ar.cause());
            }
        });
        return promise.future();
    }
}
