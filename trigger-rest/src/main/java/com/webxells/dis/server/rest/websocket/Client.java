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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.server.rest.RestTrigger;
import com.webxells.dis.server.rest.websocket.internal.WebSocketListener;
import com.webxells.dis.server.rest.websocket.transaction.WaitForResponse;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Consumer;

public class Client extends RestTrigger<WebSocketTransaction, ClientConfig> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Client.class);

    private final ClientConfig config;
    private HttpClient httpClient;
    private WebSocket webSocket;

    public Client(final ClientConfig config) {
        super(config, WaitForResponse::new);
        this.config = config;
    }

    @Override
    protected void assertRestIsUp() {
        if (null == webSocket) {
            final String uri = config.getUri();
            LOGGER.d("Creating WebSocket client: %s", uri);
            assertClientIsUp();
            buildWebSocket().buildAsync(URI.create(uri), new WebSocketListener(uri, transactionQueue, transaction))
                    .whenComplete((a, e) -> {
                if (null != e) {
                    throw new RuntimeException("Could not create websocket", e);
                }
                this.webSocket = a;
            }).join();
        }
    }

    @Override
    protected void stopRest() {
        LOGGER.d("Stopping WebSocket client");
        if (null != webSocket) {
            webSocket.abort();
            webSocket = null;
        }
    }

    @Override
    protected boolean restRunning() {
        return null != webSocket && !webSocket.isInputClosed()&& !webSocket.isOutputClosed();
    }

    private WebSocket.Builder buildWebSocket() {
        final WebSocket.Builder builder = httpClient.newWebSocketBuilder();
        setConnectionTimeout(builder::connectTimeout);
        Optional.ofNullable(config.getHeaders())
                .ifPresent(a -> a.forEach(builder::header));
        return builder;
    }

    private void setConnectionTimeout(final Consumer<Duration> consumer) {
        final ClientConfig.Timeout timeout = config.getTimeout();
        if (null != timeout && timeout.amount > 0) {
            consumer.accept(Duration.of(timeout.amount, timeout.unit));
        }
    }

    private void assertClientIsUp() {
        if (null == httpClient) {
            final HttpClient.Builder builder = HttpClient.newBuilder();
            setConnectionTimeout(builder::connectTimeout);
            httpClient = builder.build();
        }
    }
}