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
package com.webxells.dis.rest.execution;

import java.net.URI;
import java.time.temporal.ChronoUnit;

public interface Request {
    record AsyncTimer(int maxAsyncCalls, long waitTimeout) { }

    interface Builder {

        Builder uri(URI uri);

        Builder header(String key, String value);
        Builder timeout(int timeout, ChronoUnit unit);
        Builder setHeader(String key, String value);
        Builder method(HttpMethod method);
        Builder setBodyPublisher(BodyPublisher bodyPublisher);
        Builder asyncTimer(int maxAsyncCalls, long waitTimeout);
        Request build();

    }
    Headers getHeaders();
    HttpMethod getMethod();

    Request copy(BodyPublisher bodyPublisher);

    URI getUri();
    AsyncTimer asyncTimer();
    BodyPublisher getBodyPublisher();

}