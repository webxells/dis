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
package com.webxells.dis.okhttp.rest.execution;

import com.webxells.dis.rest.execution.UnmodifiableHeaders;
import java.util.List;
import java.util.Optional;
import okhttp3.Response;

public class OkHttpHeaders extends UnmodifiableHeaders {
    private final okhttp3.Response response;

    public OkHttpHeaders(final Response response) {
        this.response = response;
    }

    @Override
    public Optional<String> firstValue(final String key) {
        return response.headers(key).stream().findFirst();
    }

    @Override
    public List<String> get(final String key) {
        return response.headers(key);
    }
}