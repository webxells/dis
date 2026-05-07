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
package com.webxells.dis.test.example.rest;

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class TestRequest implements Request {
    private String path;
    private String fullPath;
    private Method method;
    private InputStream body;
    private Map<String, List<File>> files;
    private Map<String, Request> multiParts;
    private Map<String, List<String>> formParameters;
    private Map<String, List<String>> urlParameters;
    private Map<String, List<String>> headers;

    @Override
    public String getPath() {
        return path;
    }

    @Override
    public String getFullPath() {
        return fullPath;
    }

    @Override
    public Method getMethod() {
        return method;
    }

    @Override
    public InputStream getBody() {
        return body;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public Map<String, List<String>> getUrlParameters() {
        return urlParameters;
    }

    @Override
    public Map<String, List<String>> getFormParameters() {
        return formParameters;
    }

    @Override
    public Map<String, Request> getMultiParts() {
        return multiParts;
    }

    @Override
    public Map<String, List<File>> getFiles() {
        return files;
    }

    @Override
    public Response respond() {
        return null;
    }

    public void setPath(final String path) {
        this.path = path;
    }

    public void setFullPath(final String fullPath) {
        this.fullPath = fullPath;
    }

    public void setMethod(final Method method) {
        this.method = method;
    }

    public void setBody(final InputStream body) {
        this.body = body;
    }

    public void setFiles(final Map<String, List<File>> files) {
        this.files = files;
    }

    public void setMultiParts(final Map<String, Request> multiParts) {
        this.multiParts = multiParts;
    }

    public void setFormParameters(final Map<String, List<String>> formParameters) {
        this.formParameters = formParameters;
    }

    public void setUrlParameters(final Map<String, List<String>> urlParameters) {
        this.urlParameters = urlParameters;
    }

    public void setHeaders(final Map<String, List<String>> headers) {
        this.headers = headers;
    }
}