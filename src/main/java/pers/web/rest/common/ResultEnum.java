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

/**
 * <p>结果类枚举</p>
 *
 * @author liang gong
 */
public enum ResultEnum {

    /** 处理成功 */
    SUCCESS(true, 200, "successful"),
    /** 资源未找到 */
    NOT_FOUND(false, 404, "not found"),
    /** 服务器错误 */
    SERVER_ERROR(false, 500, "server error")
    ;

    private final boolean success;
    /** 响应状态码 */
    private final int code;
    /** 响应信息 */
    private final String message;

    ResultEnum(boolean success, int code, String message) {
        this.success = success;
        this.code = code;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
