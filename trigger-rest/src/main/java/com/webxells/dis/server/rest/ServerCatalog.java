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

import com.webxells.dis.server.rest.strategy.ServerEngine;
import com.webxells.dis.server.rest.strategy.ServerEngineBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerCatalog {
    public static final Map<String, ServerEngine> REGISTRY = new ConcurrentHashMap<>();

    public static synchronized ServerEngine get(final HttpServerConfig config) {
        return REGISTRY.computeIfAbsent(createKeyByListener(config.getListener()),
                a -> register(config.getServerEngine(), config.getListener()));
    }

    private static ServerEngine register(final ServerEngineBuilder builder, final Listener listener) {
        return builder
                .setPort(listener.port)
                .setBindAddress(listener.bindAddress)
                .build();
    }

    private static String createKeyByListener(final Listener listener) {
        return String.format("%d-%s", listener.port, listener.bindAddress.hash());
    }
}