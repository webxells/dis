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
package com.webxells.dis.server.rest;

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.server.rest.strategy.ServerEngineBuilder;
import com.webxells.dis.server.rest.transaction.HttpTransaction;
import java.util.Optional;

public class HttpServerConfig extends RestTriggerConfig<Request> {
    private Listener listener;
    private ServerEngineBuilder serverEngine;
    private RestMapper restMapper;

    public Listener getListener() {
        return Optional.ofNullable(listener)
                .orElseGet(Listener::new);
    }

    public void setListener(final Listener listener) {
        this.listener = listener;
    }

    @Override
    public String getType() {
        return HttpServer.class.getName();
    }

    public ServerEngineBuilder getServerEngine() {
        return serverEngine;
    }

    public void setServerEngine(final ServerEngineBuilder serverEngine) {
        this.serverEngine = serverEngine;
    }

    public RestMapper getRestFilter() {
        return restMapper;
    }

    public void setRestMapper(final RestMapper restMapper) {
        this.restMapper = restMapper;
    }
}