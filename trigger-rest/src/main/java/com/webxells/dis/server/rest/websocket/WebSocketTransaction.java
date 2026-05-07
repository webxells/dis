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
package com.webxells.dis.server.rest.websocket;

import com.webxells.dis.server.rest.websocket.internal.ByteBufferOutputStream;
import com.webxells.dis.server.rest.websocket.internal.StringOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.WebSocket;

public class WebSocketTransaction {
    private final WebSocket webSocket;
    private final InputStream request;

    public WebSocketTransaction(final WebSocket webSocket, final InputStream request) {
        this.webSocket = webSocket;
        this.request = request;
    }

    public void response(final String text) {
        webSocket.sendText(text, true).join();
    }

    public OutputStream getTextResponseStream(final int sendMessageMaxCapacity, final boolean instantFlush) {
        return new StringOutputStream(webSocket, sendMessageMaxCapacity,  instantFlush);
    }

    public OutputStream getBinaryResponseStream(final int sendMessageMaxCapacity, final boolean instantFlush) {
        return new ByteBufferOutputStream(webSocket, sendMessageMaxCapacity, instantFlush);
    }

    public InputStream getRequest() {
        return request;
    }
}