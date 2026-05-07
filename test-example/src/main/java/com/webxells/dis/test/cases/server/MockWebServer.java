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
package com.webxells.dis.test.cases.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import com.webxells.dis.test.cases.mock.MockResponse;
import com.webxells.dis.test.cases.mock.RecordedRequest;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.opentest4j.AssertionFailedError;

public class MockWebServer {
    private final Queue<MockResponse> RESPONSE_QUEUE = new ConcurrentLinkedQueue<>();
    private final Queue<RecordedRequest> REQUEST_QUEUE = new ConcurrentLinkedQueue<>();
    private final AtomicInteger requestCount = new AtomicInteger();

    protected static HttpServer server;

    public MockWebServer() {
        try {
            setUp();
        } catch (IOException e) {
            throw new AssertionFailedError("Could not start mock server", e);
        }
    }

    public void enqueue(final MockResponse response) {
        RESPONSE_QUEUE.add(response);
    }

    public RecordedRequest takeRequest() {
        return takeRequest(0, ChronoUnit.SECONDS);
    }

    public String getServerUrl(final String path) {
        final InetSocketAddress address = server.getAddress();
        return String.format("http://%s:%s/%s", address.getHostName(), address.getPort(), path);
    }

    public int getRequestCount() {
        return requestCount.get();
    }

    public void stop() {
        server.stop(0);
    }

    public void reset() {
        RESPONSE_QUEUE.clear();
        REQUEST_QUEUE.clear();
        requestCount.set(0);
    }

    public synchronized RecordedRequest takeRequest(final int delay, final ChronoUnit timeUnit) {
        final LocalDateTime till = 0 == delay ? null : LocalDateTime.now(Clock.systemUTC()).plus(delay, timeUnit);
        while (null == till || LocalDateTime.now(Clock.systemUTC()).isBefore(till)) {
            if (!REQUEST_QUEUE.isEmpty()) {
                return REQUEST_QUEUE.poll();
            }
            if (null == till) {
                break;
            }
            try {
                Thread.sleep(200);
            } catch (final InterruptedException ignored) {
                //failing anyway
            }
        }
        throw new AssertionFailedError("No recorded request in queue - you failed in time");
    }

    private void setUp() throws IOException {
        server = HttpServer.create(
                new InetSocketAddress(InetAddress.getByName("127.0.0.1").getHostAddress(), 9999),
                100);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/", this::handleExchange);
        server.start();
    }

    private void handleExchange(final HttpExchange exchange) {
        if (RESPONSE_QUEUE.isEmpty()) {
            exchange.close();
            throw new AssertionFailedError("No mocked response found");
        }
        saveRequest(exchange);
        copyResponse(exchange, Objects.requireNonNull(RESPONSE_QUEUE.poll()));
        requestCount.incrementAndGet();
        exchange.close();
    }

    private void copyResponse(final HttpExchange exchange, final MockResponse response) {
        final Headers responseHeaders = exchange.getResponseHeaders();
        final OutputStream responseBody = exchange.getResponseBody();
        final int bodySize = response.getBody().length();
        try {
            Optional.of(response.getMinWorkingTime())
                            .filter(a -> 0 < a)
                            .ifPresent(a -> {
                                try {
                                    Thread.sleep(a);
                                } catch (final InterruptedException e) {
                                    throw new AssertionFailedError("Interrupted while working... FAILED!");
                                }
                            });
            response.getHeaders()
                    .forEach((a, b) -> b.forEach(value ->  responseHeaders.add(a, value)));
            exchange.sendResponseHeaders(response.getResponseCode(), bodySize == 0 ? -1 : bodySize);
            if (0 < bodySize) {
                responseBody.write(response.getBody().getBytes(StandardCharsets.UTF_8));
                responseBody.flush();
            }
        } catch (final IOException e) {
            throw new AssertionFailedError("Could not write response", e);
        }
    }

    private void saveRequest(final HttpExchange exchange) {
        try {
            REQUEST_QUEUE.add(new RecordedRequest(
                    new String(exchange.getRequestBody().readAllBytes()),
                    exchange.getRequestMethod(),
                    getServerUrl(exchange.getRequestURI().toString().substring(1)),
                    exchange.getRequestHeaders().entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, a -> new ArrayList<>(a.getValue())))
            ));
        } catch (final IOException e) {
            throw new AssertionFailedError("Unable to read request", e);
        }
    }
}