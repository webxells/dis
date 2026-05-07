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
package com.webxells.dis.server.rest.http;

import com.sun.net.httpserver.HttpServer;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.server.rest.strategy.ServerEngine;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class JavaServer implements ServerEngine {
    private static final Logger LOGGER = LoggerProxyFactory.logger(JavaServer.class);

    private final DisHandler handler;
    private final BuiltInServer config;
    private final String description;
    private HttpServer server;
    private boolean running;

    JavaServer(final BuiltInServer config) {
        this.config = config;
        handler = new DisHandler(config);
        description = String.format("%s(port:%s)", config.getBinding().getDescription(), config.getPort());
    }

    private void startServer(final BuiltInServer config) {
        try {
            server = HttpServer.create(
                    new InetSocketAddress(config.getBinding().parse().getHostAddress(), config.getPort()),
                    config.getMaxQueueSize());
            server.setExecutor(getExecutorByWorkerThreads(config.getWorkers()));
            server.createContext(config.getRootPath(), handler);
            server.start();
            LOGGER.info("Server started on: ".concat(description));
        } catch (final IOException e) {
            throw new RuntimeException("Could not start server", e);
        }
    }

    private Executor getExecutorByWorkerThreads(final int workers) {
        if (1 == workers) {
            return Executors.newSingleThreadExecutor();
        }
        if (1 < workers) {
            return Executors.newScheduledThreadPool(workers);
        }
        return Executors.newCachedThreadPool();
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    @Override
    public synchronized void register(final RestMapper restMapper, final Consumer<Request> consumer) {
        if (!running) {
            startServer(config);
            running = true;
        }
        handler.register(restMapper, consumer);
    }

    @Override
    public synchronized void drop(final RestMapper restMapper) {
        handler.drop(restMapper);
        if (handler.isEmpty()) {
            server.stop(0);
            running = false;
            LOGGER.info("Server stopped");
        }
    }
}