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

import com.webxells.dis.server.rest.binding.Binding;
import com.webxells.dis.server.rest.strategy.ServerEngineBuilder;
import java.util.Map;

public class BuiltInServer implements ServerEngineBuilder {
    private int port;
    private Binding binding;
    private int maxQueueSize = 100;
    private int workers = 0;
    private String rootPath = "/";
    private boolean saveRequestToFile;
    private String requestContentTmpDirectory = System.getProperty("java.io.tmpdir");
    private Map<String, String> responseHeaders;

    @Override
    public BuiltInServer setPort(final int port) {
        this.port = port;
        return this;
    }

    @Override
    public BuiltInServer setBindAddress(final Binding binding) {
        this.binding = binding;
        return this;
    }

    @Override
    public JavaServer build() {
        return new JavaServer(this);
    }

    public boolean isSaveRequestToFile() {
        return saveRequestToFile;
    }

    public void setSaveRequestToFile(final boolean saveRequestToFile) {
        this.saveRequestToFile = saveRequestToFile;
    }

    public String getRequestContentTmpDirectory() {
        return requestContentTmpDirectory;
    }

    public void setRequestContentTmpDirectory(final String requestContentTmpDirectory) {
        this.requestContentTmpDirectory = requestContentTmpDirectory;
    }

    public int getPort() {
        return port;
    }

    public Binding getBinding() {
        return binding;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(final int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public int getWorkers() {
        return workers;
    }

    public void setWorkers(final int workers) {
        this.workers = workers;
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(final String rootPath) {
        this.rootPath = rootPath;
    }

    public Map<String, String> getResponseHeaders() {
        return responseHeaders;
    }

    public void setResponseHeaders(final Map<String, String> responseHeaders) {
        this.responseHeaders = responseHeaders;
    }
}