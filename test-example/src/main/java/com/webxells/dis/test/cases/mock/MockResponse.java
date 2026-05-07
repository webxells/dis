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
package com.webxells.dis.test.cases.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MockResponse extends HttpData {
    private long minWorkingTime;

    public int getResponseCode() {
        return responseCode;
    }

    public MockResponse setMinWorkingTime(final long minWorkingTime) {
        this.minWorkingTime = minWorkingTime;
        return this;
    }

    public MockResponse setBody(final String body) {
        this.body = body;
        return this;
    }
    public MockResponse setResponseCode(final int responseCode) {
        this.responseCode = responseCode;
        return this;
    }

    public MockResponse setAllHeaders(final Map<String, List<String>> headers) {
        setFullHeaders(headers);
        return this;
    }

    public MockResponse setHeaders(final Map<String, String> headers) {
        this.headers.clear();
        this.headers.putAll(headers.entrySet()
                .stream()
                .collect(Collectors.toMap(a -> a.getKey().toLowerCase(), a-> List.of(a.getValue()))));
        return this;
    }

    public MockResponse setHeader(final String key, Object value) {
        headers.computeIfAbsent(key.toLowerCase(), a -> new ArrayList<>()).add(String.valueOf(value));
        return this;
    }

    public MockResponse addHeader(final String key, Object value) {
        return setHeader(key, value);
    }


    public MockResponse setHeader(final String key, String value) {
        headers.computeIfAbsent(key.toLowerCase(), a -> new ArrayList<>()).add(value);
        return this;
    }

    public MockResponse setHeader(final String key, List<String> value) {
        headers.put(key.toLowerCase(), new ArrayList<>(value));
        return this;
    }

    public long getMinWorkingTime() {
        return minWorkingTime;
    }
}