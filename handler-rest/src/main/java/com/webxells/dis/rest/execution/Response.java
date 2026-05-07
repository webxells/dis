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
package com.webxells.dis.rest.execution;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.Optional;

public class Response {
    private final int statusCode;
    private final Headers headers;
    private final URI uri;
    private final InputStream body;

    public Response(final int statusCode, final Headers headers, final URI uri, final InputStream body) {
        this.statusCode = statusCode;
        this.headers = headers;
        this.uri = uri;
        this.body = body;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Headers getHeaders() {
        return headers;
    }

    public URI getUri() {
        return uri;
    }

    public InputStream body() {
        return body;
    }

    public Optional<String> getFullBody() {
        return Optional.ofNullable(body)
                .map(a -> {
                    try {
                        return new String(a.readAllBytes());
                    } catch (final IOException ignored) {
                        return null;
                    }
                });

    }
}