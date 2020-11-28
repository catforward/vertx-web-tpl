package pers.web.rest.s2;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import pers.web.rest.common.ResultEnum;
import pers.web.rest.common.ResultMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pers.web.rest.Symbols;

import static pers.web.rest.Symbols.ServiceAddress.S1_SERVICE;

public class S2Service extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(S2Service.class);
    @Override
    public void start() throws Exception {
        super.start();
        EventBus bus = vertx.eventBus();
        bus.consumer(Symbols.ServiceAddress.S2_SERVICE, this::handleS2Service);
        logger.info("S2Service-> deploymentId::{}", deploymentID());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
    }


    private void handleS2Service(Message<JsonObject> msg) {

        vertx.eventBus().request(S1_SERVICE, null, (AsyncResult<Message<JsonObject>> res) -> {
            if (res.failed()) {
                ResultMsg response = new ResultMsg(ResultEnum.SERVER_ERROR);
                response.setMessage(res.cause().getMessage());
                msg.reply(response.toJsonObject());
            } else {
                ResultMsg response = new ResultMsg(ResultEnum.SUCCESS);
                JsonObject ret = new JsonObject().mergeIn(res.result().body());
                ret.put("from", "request from S1");
                response.setData(ret);
                msg.reply(response.toJsonObject());
            }
        });
    }
}
