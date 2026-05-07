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
import com.webxells.dis.server.rest.strategy.ServerEngine;
import com.webxells.dis.server.rest.transaction.Accept;
import java.io.IOException;

public class HttpServer extends RestTrigger<Request, HttpServerConfig> {
    private final HttpServerConfig config;
    private final ServerEngine serverEngine;

    public HttpServer(final HttpServerConfig config) {
        super(config, Accept::new);
        this.config = config;
        serverEngine = ServerCatalog.get(config);
    }

    @Override
    protected synchronized void assertRestIsUp() {
        if (!isRunning.get()) {
            serverEngine.register(config.getRestFilter(), this::triggerRunnable);
        }
    }

    @Override
    protected void stopRest() {
        serverEngine.drop(config.getRestFilter());
    }

    @Override
    protected boolean restRunning() {
        return serverEngine.isRunning();
    }

    private void triggerRunnable(final Request request) {
        final boolean success = addToQueue(request);
        if (!success) {
            try {
                request.respond().send(429);
            } catch (final IOException e) {
                throw new RuntimeException("could not send queue is full response", e);
            }
        }
    }
}