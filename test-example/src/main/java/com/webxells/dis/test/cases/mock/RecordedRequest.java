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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class RecordedRequest extends HttpData {
    public RecordedRequest(String body, String method, String url, Map<String, List<String>> headers) {
        this.body = body;
        this.url = url;
        this.method = method;
        setFullHeaders(headers);
    }

    public List<String> getHeaders(String name) {
        return Collections.unmodifiableList(headers.get(name.toLowerCase()));
    }

    public String getHeader(String name) {
        return Optional.ofNullable(headers.get(name.toLowerCase()))
                .map(List::getFirst)
                .orElse(null);
    }
}