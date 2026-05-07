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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.rest.execution.AsyncLimiter;
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.HttpAsyncPromise;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.rest.execution.ReturnPromise;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.atomic.AtomicInteger;

public class HttpClient implements Client {
    private static class HttpClientAsyncLimiter extends AsyncLimiter {
        protected HttpClientAsyncLimiter(final Request request) {
            super(request);
        }
    }
    private static final Logger LOGGER = LoggerProxyFactory.logger(HttpClient.class);

    private final java.net.http.HttpClient httpClient;

    static Response createResponse(HttpResponse<InputStream> response) {
        return new Response(response.statusCode(), new HttpClientHeaders(response.headers()), response.uri(),
                response.body());
    }

    HttpClient(final java.net.http.HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public Request.Builder newRequest() {
        return new HttpClientRequestBuilder(this);
    }

    @Override
    public HttpVersion version() {
        switch (httpClient.version()) {
            case HTTP_1_1:
                return HttpVersion.HTTP_1_1;
            case HTTP_2:
                return HttpVersion.HTTP_2;
            default:
                throw new RuntimeException("Invalid http version by client: ".concat(httpClient.version().toString()));
        }
    }

    @Override
    public Response send(final Request request) throws IOException, InterruptedException {
        assertValidRequest(request);
        return createResponse(send(((HttpClientRequest) request).getHttpRequest()));
    }

    @Override
    public ReturnPromise sendAsync(final Request request) {
        assertValidRequest(request);
        return new HttpClientAsyncLimiter(request).send(new HttpAsyncPromise() {
            @Override
            protected void sendAndFinishWithHandleResponse() {
                LOGGER.d("Querying async (%s): %s", request.getMethod().name(), request.getUri());
                    httpClient.sendAsync(((HttpClientRequest) request).getHttpRequest()
                                    , HttpResponse.BodyHandlers.ofInputStream())
                            .thenAccept(a -> handleResponse(HttpClient.createResponse(a)));
            }
        });
    }

    private HttpResponse<InputStream> send(final HttpRequest httpRequest) throws IOException, InterruptedException {
        return httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofInputStream());
    }

    private void assertValidRequest(final Request request) {
        if (!(request instanceof HttpClientRequest)) {
            //@todo: workaround?
            throw new IllegalArgumentException("HttpClientRequest expected");
        }
    }
}