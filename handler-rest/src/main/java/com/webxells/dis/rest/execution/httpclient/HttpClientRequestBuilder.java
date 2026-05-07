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
package com.webxells.dis.rest.execution.httpclient;

import com.webxells.dis.rest.execution.BodyPublisher;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import java.net.URI;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class HttpClientRequestBuilder implements Request.Builder {
    private final HttpRequest.Builder intern;
    private final HttpClient httpClient;
    private HttpMethod method;
    private HttpRequest.BodyPublisher httpClientBodyPublisher = HttpRequest.BodyPublishers.noBody();
    private BodyPublisher bodyPublisher;
    private Request.AsyncTimer asyncTimer;

    public HttpClientRequestBuilder(final HttpClient httpClient) {
        this.httpClient = httpClient;
        intern = HttpRequest.newBuilder();
    }

    @Override
    public Request.Builder setBodyPublisher(final BodyPublisher bodyPublisher) {
        httpClientBodyPublisher = new HttpClientBodyPublisher(bodyPublisher);
        this.bodyPublisher = bodyPublisher;
        return this;
    }

    @Override
    public Request.Builder asyncTimer(final int maxAsyncCalls, final long waitTimeout) {
        this.asyncTimer = new Request.AsyncTimer(maxAsyncCalls, waitTimeout);
        return this;
    }

    @Override
    public HttpClientRequestBuilder uri(final URI uri) {
        intern.uri(uri);
        return this;
    }

    @Override
    public HttpClientRequestBuilder header(final String name, final String value) {
        intern.header(name, value);
        return this;
    }

    @Override
    public Request.Builder timeout(final int timeout, final ChronoUnit unit) {
        intern.timeout(Duration.of(timeout, unit));
        return this;
    }

    @Override
    public HttpClientRequestBuilder setHeader(final String name, final String value) {
        intern.setHeader(name, value);
        return this;
    }

    @Override
    public Request build() {
        if (null != method) {
            intern.method(method.name(), httpClientBodyPublisher);
        }
        intern.version(java.net.http.HttpClient.Version.valueOf(httpClient.version().toString()));
        return new HttpClientRequest(intern.build(), bodyPublisher, asyncTimer);
    }

    public HttpClientRequestBuilder method(final HttpMethod method) {
        this.method = method;
        return this;
    }

    private void setHttpClientBodyPublisher(final HttpRequest.BodyPublisher httpClientBodyPublisher) {
        if (null != httpClientBodyPublisher) {
            this.httpClientBodyPublisher = httpClientBodyPublisher;
        }
    }
}