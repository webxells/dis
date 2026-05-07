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

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Request.Method;
import com.webxells.dis.api.rest.filter.RequestFilter;
import com.webxells.dis.localfile.input.LocalPreSavedInputStream;
import com.webxells.dis.localfile.resource.PreSavedTempFile;
import com.webxells.dis.server.rest.strategy.RestMapper;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DisHandler implements HttpHandler {
    private final BuiltInServer config;
    private final Map<RestMapper, Consumer<Request>> registry = new HashMap<>();
    private final String requestContentTmpDirectory;

    public DisHandler(final BuiltInServer config) {
        this.config = config;
        requestContentTmpDirectory = config.getRequestContentTmpDirectory();
    }

    @Override
    public void handle(final HttpExchange exchange) throws IOException {
        final Map.Entry<RestMapper, Consumer<Request>> foundEntry = find(exchange.getRequestMethod(), exchange.getRequestURI());
        setResponseHeaders(exchange);
        if (null == foundEntry) {
            send(exchange, 404);
            return;
        }
        final DisRequest disRequest = new DisRequest(exchange, getBodyStream(exchange.getRequestBody()));
        final Optional<RequestFilter.ReturnState> filterState = Optional.ofNullable(foundEntry.getKey().getRequestFilters())
                .flatMap(a -> a.stream().flatMap(b -> b.resolve(disRequest).stream()).findAny());
        if (filterState.isPresent()) {
            send(exchange, filterState.get().getHttpState());
            return;
        }
        foundEntry.getValue().accept(disRequest);
    }

    private void setResponseHeaders(final HttpExchange exchange) {
        final Headers headers = exchange.getResponseHeaders();
        Optional.ofNullable(config.getResponseHeaders())
                .ifPresent(a -> a.forEach(headers::add));
    }

    private void send(final HttpExchange exchange, final int status) throws IOException {
        exchange.sendResponseHeaders(status, 0);
        exchange.close();
    }

    private LocalPreSavedInputStream getBodyStream(final InputStream inputStream) throws IOException {
        return toTmpFile(inputStream);
    }

    private LocalPreSavedInputStream toTmpFile(final InputStream inputStream) throws IOException {
        try {
            return PreSavedTempFile.createPreSavedInputStream(inputStream, requestContentTmpDirectory);
        } catch (final InputOutputError e) {
            throw new IOException(e);
        }
    }

    private Map.Entry<RestMapper, Consumer<Request>> find(final String requestMethod, final URI requestURI) {
        return registry.entrySet().stream()
                .filter(a -> matchesMethod(a.getKey().getMethod(), requestMethod) && matchesPath(a.getKey().getPath(), requestURI))
                .findAny()
                .orElse(null);

    }

    private boolean matchesPath(final String path, final URI requestURI) {
        return requestURI.getPath().equals(path);
    }

    private boolean matchesMethod(final Method method, final String requestMethod) {
        return Method.valueOf(requestMethod) == method;
    }

    public void register(final RestMapper restMapper, final Consumer<Request> consumer) {
        registry.put(restMapper, consumer);
    }

    public void drop(final RestMapper restMapper) {
        registry.remove(restMapper);
    }

    public boolean isEmpty() {
        return registry.isEmpty();
    }
}