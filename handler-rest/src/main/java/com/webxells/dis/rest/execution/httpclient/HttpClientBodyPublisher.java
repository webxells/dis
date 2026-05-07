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

import com.webxells.dis.rest.execution.BodyPublisher;
import java.net.http.HttpRequest;
import java.nio.ByteBuffer;
import java.util.concurrent.Flow;

public class HttpClientBodyPublisher implements HttpRequest.BodyPublisher {
    private final BodyPublisher bodyPublisher;
    private final HttpRequest.BodyPublisher httpClientBodyPublisher;

    public HttpClientBodyPublisher(final BodyPublisher bodyPublisher) {
        this.bodyPublisher = bodyPublisher;
        httpClientBodyPublisher = HttpRequest.BodyPublishers.ofInputStream(bodyPublisher::get);;
    }

    @Override
    public long contentLength() {
        return bodyPublisher.contentLength();
    }

    @Override
    public void subscribe(final Flow.Subscriber<? super ByteBuffer> subscriber) {
        httpClientBodyPublisher.subscribe(subscriber);
    }
}