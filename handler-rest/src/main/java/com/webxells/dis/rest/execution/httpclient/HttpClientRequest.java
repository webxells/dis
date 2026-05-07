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
import com.webxells.dis.rest.execution.Headers;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.Request;
import java.net.URI;
import java.net.http.HttpRequest;

public class HttpClientRequest implements Request {
    private final HttpRequest request;
    private final BodyPublisher bodyPublisher;
    private final AsyncTimer asyncTimer;

    public HttpClientRequest(final HttpRequest request, final BodyPublisher bodyPublisher,
                             final AsyncTimer asyncTimer) {
        this.request = request;
        this.bodyPublisher = bodyPublisher;
        this.asyncTimer = asyncTimer;
    }

    public HttpRequest getHttpRequest() {
        return request;
    }

    @Override
    public Headers getHeaders() {
        return new HttpClientHeaders(request.headers());
    }

    @Override
    public HttpMethod getMethod() {
        return HttpMethod.valueOf(request.method());
    }

    @Override
    public Request copy(final BodyPublisher bodyPublisher) {
        return new HttpClientRequest(request, bodyPublisher, asyncTimer);
    }

    @Override
    public URI getUri() {
        return request.uri();
    }

    @Override
    public AsyncTimer asyncTimer() {
        return asyncTimer;
    }

    @Override
    public BodyPublisher getBodyPublisher() {
        return bodyPublisher;
    }
}