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
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import com.webxells.dis.server.rest.intern.BodyStreamBuffer;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class DisResponse implements Response {
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";

    private final DisRequest disRequest;
    private final HttpExchange httpExchange;
    private final Map<String, String> headers = new HashMap<>();
    private final BodyStreamBuffer body;

    private int status;
    private ResponseState state = ResponseState.UNHANDLED;

    public DisResponse(final DisRequest disRequest) {
        this.disRequest = disRequest;
        httpExchange = disRequest.getExchange();
        body = new BodyStreamBuffer(this, httpExchange.getResponseBody());
    }

    @Override
    public void setStatus(final int status) {
        this.status = status;
    }

    @Override
    public void setHeaders(final Map<String, String> headers) {
        this.headers.putAll(headers);
    }

    @Override
    public void addHeader(final String key, final String value) {
        headers.put(key, value);
    }

    @Override
    public ResponseState state() {
        return state;
    }

    @Override
    public void sendHeaders() throws IOException {
        if (state != ResponseState.UNHANDLED) {
            throw new IOException("Headers already sent");
        }
        if (!headers.containsKey("content-type") && !httpExchange.getResponseHeaders().containsKey("content-type")) {
            headers.put("content-type", DEFAULT_CONTENT_TYPE);
        }
        final Headers responseHeaders = httpExchange.getResponseHeaders();
        headers.forEach(responseHeaders::add);
        httpExchange.sendResponseHeaders(status, getContentSize(body.getLength()));
        state = ResponseState.HEADERS_SENT;
    }

    private long getContentSize(final long length) {
        return length == 0 ? -1 : length;
    }

    @Override
    public void send() throws IOException {
        switch (state) {
            case SENT:
                throw new IOException("Already sent");
            case UNHANDLED:
                sendHeaders();
        }
        body.writeToBody();
        httpExchange.close();
        state = ResponseState.SENT;
    }

    @Override
    public void send(final int httpStatus) throws IOException {
        setStatus(httpStatus);
        sendHeaders();
        send();
    }

    @Override
    public OutputStream getBody() {
        return body;
    }

    @Override
    public Request getRequest() {
        return disRequest;
    }


}