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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.server.rest.Transaction;
import com.webxells.dis.server.rest.Transaction.Entity;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.websocket.WebSocketTransaction;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class WebSocketListener implements WebSocket.Listener {
    private static final Logger LOGGER = LoggerProxyFactory.logger(WebSocketListener.class.getName());

    private final String uri;
    private final TransactionQueue<Entity<WebSocketTransaction>> transactionTransactionQueue;
    private final Transaction<WebSocketTransaction> transaction;
    private final StringBuilder textBuilder = new StringBuilder();

    public WebSocketListener(final String uri, final TransactionQueue<Entity<WebSocketTransaction>> transactionTransactionQueue,
                             final Transaction<WebSocketTransaction> transaction) {
        this.uri = uri;
        this.transactionTransactionQueue = transactionTransactionQueue;
        this.transaction = transaction;
    }

    @Override
    public void onOpen(final WebSocket webSocket) {
        LOGGER.d("Listening to websocket: ".concat(uri));
        next(webSocket);
    }

    @Override
    public CompletionStage<?> onText(final WebSocket webSocket, final CharSequence data, final boolean last) {
        LOGGER.d("Got some %s text data... (%s)%nContent: %s", last ? "last" : "continuous", uri, data);
        textBuilder.append(data);
        next(webSocket);
        if (!last) {
            return null;
        }
        final String result = textBuilder.toString();
        textBuilder.setLength(0);
        return CompletableFuture.supplyAsync(() -> queueData(webSocket,
                new ByteArrayInputStream(result.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public CompletionStage<?> onBinary(final WebSocket webSocket, final ByteBuffer data, final boolean last) {
        LOGGER.d("Got some binary data... (%s)%nBinary with size of %d", uri, data.remaining());
        next(webSocket);
        //return CompletableFuture.supplyAsync(() -> queueData(webSocket, new ByteBufferInputStream(data)));
        LOGGER.w("binary websocket receiving not yet implemented - skipping...");
        return null;
    }

    @Override
    public CompletionStage<?> onPing(final WebSocket webSocket, final ByteBuffer message) {
        LOGGER.t("receiving a ping requested: ".concat(uri));
        next(webSocket);
        return CompletableFuture.completedFuture("ping completed")
                .thenAccept(LOGGER::t);
    }

    @Override
    public CompletionStage<?> onPong(final WebSocket webSocket, final ByteBuffer message) {
        LOGGER.t("sending a pong response: ".concat(uri));
        next(webSocket);
        return CompletableFuture.completedFuture("pong completed")
                .thenAccept(LOGGER::t);
    }

    @Override
    public void onError(final WebSocket webSocket, final Throwable error) {
        LOGGER.t("error occurred on websocket: %s%naborting...", error, uri);
        webSocket.abort();
    }

    @Override
    public CompletionStage<?> onClose(final WebSocket webSocket, final int statusCode, final String reason) {
        LOGGER.d("websocket closed: %s%nstatus %d, reason: %s", webSocket, statusCode, reason);
        return CompletableFuture.completedFuture("Successfully closed")
                .thenAccept(LOGGER::t);
    }

    private void next(final WebSocket webSocket) {
        webSocket.request(1);
    }

    private boolean queueData(final WebSocket webSocket, final InputStream inputStream) {
        transactionTransactionQueue.add(transaction.newEntity(new WebSocketTransaction(webSocket, inputStream)));
        return true;
    }
}