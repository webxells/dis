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


import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Callable;

public class BodyPublisherOfInputStream implements BodyPublisher {
    private final Callable<InputStream> streamCallable;
    private final long size;
    private InputStream actualStream;

    public static BodyPublisherOfInputStream byString(final String body) {
        final byte[] bytes = body.getBytes();
        return new BodyPublisherOfInputStream(() -> new ByteArrayInputStream(bytes), bytes.length);
    }

    public BodyPublisherOfInputStream(final Callable<InputStream> streamCallable, final long size) {
        this.streamCallable = streamCallable;
        this.size = size;
    }

    @Override
    public long contentLength() {
        return size;
    }

    @Override
    public InputStream get() {
        return getStream();
    }

    private InputStream getStream() {
        if (null == actualStream) {
            try {
                actualStream = streamCallable.call();
            } catch (final Exception e) {
                throw new RuntimeException("could not collect InputStream", e);
            }
        } else {
            try {
                actualStream.reset();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return actualStream;
    }
}