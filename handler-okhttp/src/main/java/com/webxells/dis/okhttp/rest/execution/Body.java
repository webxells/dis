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

import com.webxells.dis.rest.execution.BodyPublisher;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okio.BufferedSink;

public class Body extends RequestBody {
    private final BodyPublisher bodyPublisher;
    private Request request;

    public Body(final BodyPublisher bodyPublisher) {
        this.bodyPublisher = bodyPublisher;
    }

    public void setRequest(final Request request) {
        this.request = request;
    }

    @Override
    public MediaType contentType() {
        return Optional.ofNullable(request.header("content-type"))
                .map(MediaType::get)
                .orElse(null);
    }

    @Override
    public void writeTo(final BufferedSink bufferedSink) throws IOException {
        Optional.ofNullable(bodyPublisher)
                .map(BodyPublisher::get)
                .ifPresent(a -> readIntoSink(a, bufferedSink));
    }

    private void readIntoSink(final InputStream a, final BufferedSink bufferedSink) {
        byte[] current = new byte[2048];
        int read;
        try {
            while((read = a.read(current)) > -1) {
                bufferedSink.write(read < 2048 ? Arrays.copyOf(current, read) : current);
            }
        } catch (final IOException e) {
            throw new RuntimeException("could not write to body", e);
        }
    }

    public BodyPublisher getBodyPublisher() {
        return bodyPublisher;
    }
}