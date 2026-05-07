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
package com.webxells.dis.server.rest.transaction;

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class WaitingRequest implements Request {

    public class TimeoutResponse implements Response {

        public class IgnoreInputAfterTimeoutStream extends OutputStream {
            private final OutputStream body;
            public IgnoreInputAfterTimeoutStream() {
                body = response.getBody();
            }

            @Override
            public void write(final int b) throws IOException {
                if (inTime()) {
                    body.write(b);
                }
            }

            @Override
            public void flush() throws IOException {
                if (inTime()) {
                    body.flush();
                }
            }

            @Override
            public void close() throws IOException {
                if (inTime()) {
                    body.close();
                }
            }
        }

        private final Response response;
        private final IgnoreInputAfterTimeoutStream body;

        public TimeoutResponse() {
            this.response = request.respond();
            body = new IgnoreInputAfterTimeoutStream();
        }

        @Override
        public void setStatus(final int status) {
            response.setStatus(status);
        }

        @Override
        public void setHeaders(final Map<String, String> headers) {
            response.setHeaders(headers);
        }

        @Override
        public void addHeader(final String key, final String value) {
            response.addHeader(key, value);
        }

        @Override
        public ResponseState state() {
            return response.state();
        }

        @Override
        public void sendHeaders() throws IOException {
            response.sendHeaders();
        }

        @Override
        public void send() throws IOException {
            clearTimeout();
            response.send();
        }

        @Override
        public void send(final int status) throws IOException {
            clearTimeout();
            response.send(status);
        }

        @Override
        public OutputStream getBody() {
            return body;
        }

        @Override
        public Request getRequest() {
            return request;
        }

        private void clearTimeout() {
            if (null != timeout && !dontInterrupt && timeout.isAlive()) {
                timeout.interrupt();
            }
        }
    }

    private final Request request;
    private final Thread timeout;
    private final TimeoutResponse response;

    private boolean dontInterrupt;

    public WaitingRequest(final Request request, final int timeout) {
        this.request = request;
        this.timeout = startTimeoutThread(timeout);
        response = new TimeoutResponse();
    }

    private Thread startTimeoutThread(final int timeout) {
        if (0 < timeout) {
            final Thread result = new Thread(() -> {
                try {
                    Thread.sleep(timeout * 1000L);
                } catch (final InterruptedException ignored) { }
                try {
                    if (Response.ResponseState.SENT != request.respond().state()) {
                        dontInterrupt = true;
                        response.send(504);
                    }
                } catch (final IOException e) {
                    throw new RuntimeException("could not send timeout response", e);
                }
            });
            result.start();
            return result;
        }
        return null;
    }

    @Override
    public String getPath() {
        return request.getPath();
    }

    @Override
    public String getFullPath() {
        return request.getFullPath();
    }

    @Override
    public Method getMethod() {
        return request.getMethod();
    }

    @Override
    public InputStream getBody() {
        return request.getBody();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public Map<String, List<String>> getUrlParameters() {
        return request.getUrlParameters();
    }

    @Override
    public Map<String, List<String>> getFormParameters() {
        return request.getFormParameters();
    }

    @Override
    public Map<String, Request> getMultiParts() {
        return request.getMultiParts();
    }

    @Override
    public Map<String, List<File>> getFiles() {
        return request.getFiles();
    }

    @Override
    public Response respond() {
        return response;
    }

    private boolean inTime() {
        return null == timeout || timeout.isAlive();
    }
}