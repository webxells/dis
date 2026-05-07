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

import com.sun.net.httpserver.HttpExchange;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import com.webxells.dis.localfile.input.LocalPreSavedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DisRequest implements Request {
    private final HttpExchange exchange;
    private final Response response;
    private final LocalPreSavedInputStream bodyStream;

    public DisRequest(final HttpExchange exchange, final LocalPreSavedInputStream bodyStream) {
        this.exchange = exchange;
        this.bodyStream = bodyStream;
        response = new DisResponse(this);
    }

    @Override
    public void clear() {
        try {
            bodyStream.close();
        } catch (final IOException e) {
            throw new RuntimeException("Could not close body source file", e);
        }
    }

    @Override
    public Response respond() {
        return response;
    }

    @Override
    public String getPath() {
        return exchange.getRequestURI().getPath();
    }

    @Override
    public String getFullPath() {
        return getPath().concat(
                Optional.ofNullable(exchange.getRequestURI().getQuery())
                        .map("?"::concat)
                        .orElse(""));
    }

    @Override
    public Method getMethod() {
        return Method.valueOf(exchange.getRequestMethod());
    }

    @Override
    public InputStream getBody() {
        try {
            return bodyStream.isOpen() ? bodyStream.getCopy() : null;
        } catch (final IOException e) {
            throw new RuntimeException("Could not create copy", e);
        }
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return exchange.getRequestHeaders();
    }

    @Override
    public Map<String, List<String>> getUrlParameters() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Map<String, List<String>> getFormParameters() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Map<String, Request> getMultiParts() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    @Override
    public Map<String, List<File>> getFiles() {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public HttpExchange getExchange() {
        return exchange;
    }
}