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

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import com.webxells.dis.server.rest.binding.Binding;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.server.rest.strategy.ServerEngine;
import com.webxells.dis.server.rest.strategy.ServerEngineBuilder;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class TestServerBuilder implements ServerEngineBuilder {

    public static class TestResponse implements Response {

        @Override
        public void setStatus(final int status) {

        }

        @Override
        public void setHeaders(final Map<String, String> headers) {

        }

        @Override
        public void addHeader(final String key, final String value) {

        }

        @Override
        public ResponseState state() {
            return null;
        }

        @Override
        public void sendHeaders() throws IOException {

        }

        @Override
        public void send() throws IOException {

        }

        @Override
        public void send(final int status) throws IOException {

        }

        @Override
        public OutputStream getBody() {
            return null;
        }

        @Override
        public Request getRequest() {
            return null;
        }
    }
    public static class TestRequest implements Request {

        @Override
        public String getPath() {
            return null;
        }

        @Override
        public String getFullPath() {
            return null;
        }

        @Override
        public Method getMethod() {
            return null;
        }

        @Override
        public InputStream getBody() {
            return null;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return null;
        }

        @Override
        public Map<String, List<String>> getUrlParameters() {
            return null;
        }

        @Override
        public Map<String, List<String>> getFormParameters() {
            return null;
        }

        @Override
        public Map<String, Request> getMultiParts() {
            return null;
        }

        @Override
        public Map<String, List<File>> getFiles() {
            return null;
        }

        @Override
        public Response respond() {
            return new TestResponse();
        }
    }
    public static class TestServer implements ServerEngine {
        private boolean running;
        private Consumer<Request> consumer;

        @Override
        public boolean isRunning() {
            return running;
        }

        @Override
        public void register(final RestMapper restMapper, final Consumer<Request> consumer) {
            running = true;
            this.consumer = consumer;
        }

        @Override
        public void drop(final RestMapper restMapper) {
            running = false;
        }

        public void triggerRequest() {
            consumer.accept(new TestRequest());
        }
    }
    private static final TestServer INSTANCE = new TestServer();
    @Override
    public ServerEngineBuilder setPort(final int port) {
        return this;
    }

    @Override
    public ServerEngineBuilder setBindAddress(final Binding binding) {
        return this;
    }

    @Override
    public TestServer build() {
        return INSTANCE;
    }

}