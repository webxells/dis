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

import com.webxells.dis.rest.execution.UnmodifiableHeaders;
import java.net.http.HttpHeaders;
import java.util.List;
import java.util.Optional;

public class HttpClientHeaders extends UnmodifiableHeaders {
    private final HttpHeaders headers;

    public HttpClientHeaders(final HttpHeaders headers) {
        this.headers = headers;
    }

    @Override
    public void add(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final String key, final String value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(final String key, final List<String> value) {
        throw new UnsupportedOperationException();
    }

    public Optional<String> firstValue(final String key) {
        return headers.firstValue(key);
    }

    public List<String> get(final String key) {
        return headers.allValues(key);
    }

}