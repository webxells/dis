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
package com.webxells.dis.rest.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class RestOutputStream extends OutputStream {
    private final boolean emptyFlush;
    private final Consumer<RestOutputStream> consumer;
    private ByteArrayOutputStream content = new ByteArrayOutputStream();

    public RestOutputStream(final boolean emptyFlush, final Consumer<RestOutputStream> consumer) {
        this.emptyFlush = emptyFlush;
        this.consumer = consumer;
    }

    public String getContent() {
        return content.toString(StandardCharsets.UTF_8);
    }

    @Override
    public void write(final int b) {
        content.write(b);
    }

    @Override
    public void flush() throws IOException {
        if (content.size() > 0 || emptyFlush) {
            try {
                consumer.accept(this);
            } catch (Exception e) {
                throw new IOException("Flush failed", e);
            }
            content = new ByteArrayOutputStream();
        }
    }

    @Override
    public void close() throws IOException {
        content.close();
    }
}