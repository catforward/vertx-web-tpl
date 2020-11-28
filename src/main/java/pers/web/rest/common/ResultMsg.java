/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pers.web.rest.common;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Objects;

/**
 * <pre>
 * ServiceVerticle的返回结果
 *
 * 正常时：
 * {
 *    "success": true,
 *    "code": 200,
 *    "message": "",
 *    "data": {
 *        "key": "value",
 *        ...
 *    }
 * }
 *
 * 异常时：
 * {
 *     "success": false,
 *     "code": 404,
 *     "message": "not found",
 *     "data": {}
 * }
 * </pre>
 *
 * @author liang gong
 */
public final class ResultMsg {
    /** 响应是否成功 */
    private final boolean success;
    /** 响应状态码 */
    private final int code;
    /** 响应信息(请求ID) */
    private String message;
    /** 响应结果数据 */
    private JsonObject data = new JsonObject();

    public ResultMsg() {
        this.success = true;
        this.message = "";
        this.code = 200;
    }

    public ResultMsg(String message) {
        this.success = true;
        this.message = message;
        this.code = 200;
    }

    public ResultMsg(ResultEnum result) {
        this.success = result.isSuccess();
        this.code = result.getCode();
        this.message = result.getMessage();
    }

    public ResultMsg setData(JsonObject jsonObject) {
        this.data.mergeIn(jsonObject);
        return this;
    }

    public ResultMsg setArrayData(String key, JsonArray jsonArray) {
        this.data.put(key, jsonArray);
        return this;
    }

    public ResultMsg setMessage(String msg) {
        this.message = Objects.requireNonNull(msg);
        return this;
    }

    /**
     * 返回此数据的JSON对象
     *
     * @return String
     */
    public JsonObject toJsonObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.put("success", this.success);
        jsonObject.put("code", this.code);
        jsonObject.put("message", this.message);
        jsonObject.put("data", this.data);
        return jsonObject;
    }
}
