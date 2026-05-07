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

import java.net.http.WebSocket;
import java.nio.ByteBuffer;

public class ByteBufferOutputStream extends WebsocketOutputStream<ByteBuffer> {

    public ByteBufferOutputStream(final WebSocket webSocket, final int capacity, final boolean instantFlush) {
        super(webSocket, capacity, instantFlush);
    }

    @Override
    void send(final ByteBuffer b, final boolean last) {
        webSocket.sendBinary(b, last);
    }

    @Override
    ByteBuffer wrap(final byte[] b, final int off, final int len) {
        return ByteBuffer.wrap(b, off, len);
    }

    @Override
    ByteBuffer empty() {
        return ByteBuffer.allocate(0);
    }
}