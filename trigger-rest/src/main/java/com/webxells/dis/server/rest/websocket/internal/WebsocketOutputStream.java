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
package com.webxells.dis.server.rest.websocket.internal;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.http.WebSocket;

abstract class WebsocketOutputStream<T> extends OutputStream {
    protected final WebSocket webSocket;
    private final int capacity;
    private final boolean instantFlush;
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream(4096);

    public WebsocketOutputStream(final WebSocket webSocket, final int capacity, final boolean instantFlush) {
        this.webSocket = webSocket;
        this.capacity = capacity;
        this.instantFlush = instantFlush;
    }

    abstract void send(final T b, final boolean last);
    abstract T wrap(final byte[] b, final int off, final int len);
    abstract T empty();

    @Override
    public void write(final int b) {
        if (instantFlush) {
            send(new byte[] {(byte) b}, false);
        } else {
            bos.write(b);
        }
    }

    @Override
    public void write(final byte[] b) throws IOException {
        if (instantFlush) {
            send(b, false);
        } else {
            bos.write(b);
        }
    }

    @Override
    public void write(final byte[] b, final int off, final int len) {
        if (instantFlush) {
            send(wrap(b, off, len), false);
        } else {
            bos.write(b, off, len);
        }
    }

    private void send(final byte[] b, final boolean last) {
        final int max = Double.valueOf(Math.ceil(((double) b.length) / capacity))
                .intValue();
        final int length = b.length;
        for (int i = 0; i < max; i++) {
            final int start = i * capacity;
            send(wrap(b, start, Math.min(capacity, length - start)), last && max == i + 1);
        }
    }

    @Override
    public void flush() {
        if (instantFlush) {
            send(empty(), true);
            return;
        }
        send(bos.toByteArray(), true);
    }

    @Override
    public void close() throws IOException {
        bos.close();
    }
}