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
package com.webxells.dis.okhttp.rest.execution;

import com.webxells.dis.rest.execution.BodyPublisher;
import com.webxells.dis.rest.execution.Headers;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import java.net.URI;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class OkHttpRequest implements Request {
    public static class Builder implements Request.Builder {
        private final okhttp3.Request.Builder request = new okhttp3.Request.Builder();
        private String method = HttpMethod.GET.name();
        private BodyPublisher bodyPublisher;
        private AsyncTimer asyncTimer;

        @Override
        public Builder uri(final URI uri) {
            request.url(uri.toString());
            return this;
        }

        @Override
        public Builder header(final String key, final String value) {
            request.addHeader(key, value);
            return this;
        }

        @Override
        public Builder timeout(final int timeout, final ChronoUnit unit) {
            return this;
        }

        @Override
        public Builder setHeader(final String key, final String value) {
            request.header(key, value);
            return this;
        }

        @Override
        public Builder method(final HttpMethod method) {
            this.method = method.name();
            return this;
        }

        public Builder method(final String method) {
            this.method = method;
            return this;
        }

        @Override
        public Builder setBodyPublisher(final BodyPublisher bodyPublisher) {
            this.bodyPublisher = bodyPublisher;
            return this;
        }

        @Override
        public Request.Builder asyncTimer(final int maxAsyncCalls, final long waitTimeout) {
            asyncTimer = new AsyncTimer(maxAsyncCalls, waitTimeout);
            return this;
        }

        @Override
        public OkHttpRequest build() {
            Body body = null;
            if (null != bodyPublisher) {
                body = new Body(bodyPublisher);
            }
            request.method(method, body);
            final okhttp3.Request result = request.build();
            Optional.ofNullable(body).ifPresent(a -> a.setRequest(result));
            return new OkHttpRequest(result, this);
        }

    }
    private final okhttp3.Request request;
    private final Builder builder;

    public OkHttpRequest(final okhttp3.Request request) {
        this(request, null);
    }

    public OkHttpRequest(final okhttp3.Request request, final Builder builder) {
        this.request = request;
        this.builder = builder;
    }


    public okhttp3.Request getRequest() {
        return request;
    }

    @Override
    public Headers getHeaders() {
        return null;//new OkHttpHeaders(request.headers());
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.method());
    }

    @Override
    public Request copy(final BodyPublisher bodyPublisher) {
        if (null != builder) {
            return builder
                    .setBodyPublisher(bodyPublisher)
                    .build();
        }
        final OkHttpRequest.Builder builder = new OkHttpRequest.Builder();
        request.headers().toMultimap()
                .forEach((a, b) ->
                        b.forEach(c -> builder.header(a, c)));
        return builder
                .method(request.method())
                .setBodyPublisher(bodyPublisher)
                .build();
    }

    @Override
    public URI getUri() {
        return request.url().uri();
    }

    @Override
    public AsyncTimer asyncTimer() {
        return builder.asyncTimer;
    }

    @Override
    public BodyPublisher getBodyPublisher() {
        //rly?
        return Optional.ofNullable((Body) request.body())
                .map(Body::getBodyPublisher)
                .orElse(null);
    }
}