/**
 * Copyright (C) 2020-2026 webXells GmbH
 *
 * This work is licensed under the Creative Commons
 * Attribution-NonCommercial-NoDerivatives 4.0 International Public License.
 *
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://creativecommons.org/licenses/by-nc-nd/4.0/
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an AS IS BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 **/
package com.webxells.dis.server.rest.websocket;

import com.webxells.dis.server.rest.RestTriggerConfig;
import java.time.temporal.ChronoUnit;
import java.util.Map;

public class ClientConfig extends RestTriggerConfig<WebSocketTransaction> {
    public static class Timeout {
        public int amount;
        public ChronoUnit unit = ChronoUnit.SECONDS;
    }

    private Timeout timeout;
    private Map<String, String> headers;
    private String uri;

    public ClientConfig() {
        super();
    }

    @Override
    public String getType() {
        return Client.class.getName();
    }

    public void setTimeout(final Timeout timeout) {
        this.timeout = timeout;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(final Map<String, String> headers) {
        this.headers = headers;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(final String uri) {
        this.uri = uri;
    }
}