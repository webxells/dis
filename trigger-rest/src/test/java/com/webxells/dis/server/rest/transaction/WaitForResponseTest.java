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
package com.webxells.dis.server.rest.transaction;

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.server.rest.HttpServer;
import com.webxells.dis.server.rest.HttpServerConfig;
import com.webxells.dis.server.rest.Listener;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.ServerOutput;
import com.webxells.dis.server.rest.http.BuiltInServer;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.OutputStream;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class WaitForResponseTest extends SimpleTestCase {

    private final HttpClient httpClient = HttpClient.newBuilder()
            .build();

    @Test
    void test500() throws InterruptedException {
        String resourceName = random();
        TransactionQueue.getQueue(resourceName).reset();

        int port = 9666;
        Listener listener = new Listener();
        listener.port = port;

        BuiltInServer serverBuilder = new BuiltInServer()
                .setBindAddress(listener.bindAddress)
                .setPort(port);

        RestMapper restMapper = new RestMapper();
        restMapper.setMethod(Request.Method.POST);
        restMapper.setPath("/");

        final HttpServerConfig config = new HttpServerConfig();
        config.setWaitBetweenCheck(0);
        config.setListener(listener);
        config.setName(resourceName);
        config.setServerEngine(serverBuilder);
        config.setRestMapper(restMapper);
        config.setTransaction(new WaitForResponse() {{
            setGatewayTimeout(900);
        }});

        HttpServer serverTrigger = new HttpServer(config);

        serverTrigger.awaitAction(() -> {});


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%s/", serverBuilder.getBinding().getDescription(), port)))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        AtomicReference<HttpResponse<String>> response = new AtomicReference<>();
        new Thread(() -> {
            try {
                response.set(httpClient.send(request, HttpResponse.BodyHandlers.ofString()));
            } catch (IOException | InterruptedException ignored) { }
        }).start();

        Thread.sleep(100);

        new Thread(() -> {
            try {
                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException ignored) { }
        }).start();

        Thread.sleep(200);

        assertNotNull(response.get());
        assertEquals(500, response.get().statusCode());
        assertTrue(response.get().body().isEmpty());

        serverTrigger.abort();

    }

    @Test
    void test504() throws IOException, InterruptedException {
        String resourceName = random();

        int port = 9666;
        Listener listener = new Listener();
        listener.port = port;

        BuiltInServer serverBuilder = new BuiltInServer()
                .setBindAddress(listener.bindAddress)
                .setPort(port);

        RestMapper restMapper = new RestMapper();
        restMapper.setMethod(Request.Method.POST);
        restMapper.setPath("/");

        final HttpServerConfig config = new HttpServerConfig();
        config.setWaitBetweenCheck(1);
        config.setListener(listener);
        config.setName(resourceName);
        config.setServerEngine(serverBuilder);
        config.setRestMapper(restMapper);
        WaitForResponse waitForResponse = new WaitForResponse();
        waitForResponse.setGatewayTimeout(1);
        config.setTransaction(waitForResponse);

        HttpServer serverTrigger = new HttpServer(config);

        serverTrigger.awaitAction(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) { }
        });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%s/", serverBuilder.getBinding().getDescription(), port)))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(504, response.statusCode());
        assertTrue(response.body().isEmpty());

        serverTrigger.abort();
    }

    @Test
    void test200() throws IOException, InterruptedException {
        String returningBody = random();
        String resourceName = random();
        TransactionQueue.getQueue(resourceName).reset();

        int port = 9666;
        Listener listener = new Listener();
        listener.port = port;

        BuiltInServer serverBuilder = new BuiltInServer()
                .setBindAddress(listener.bindAddress)
                .setPort(port);
        serverBuilder.setSaveRequestToFile(true);

        RestMapper restMapper = new RestMapper();
        restMapper.setMethod(Request.Method.POST);
        restMapper.setPath("/");

        WaitForResponse fixture = new WaitForResponse();

        final HttpServerConfig config = new HttpServerConfig();
        config.setWaitBetweenCheck(4);
        config.setListener(listener);
        config.setName(resourceName);
        config.setServerEngine(serverBuilder);
        config.setRestMapper(restMapper);
        config.setTransaction(fixture);

        HttpServer serverTrigger = new HttpServer(config);

        serverTrigger.awaitAction(() -> {
            ServerOutput response = new ServerOutput();
            response.setHeader(Map.of("content-type", "text/plain"));
            response.setName(resourceName);
            assertThrows(UnsupportedOperationException.class, response::receive);
            try {
                final OutputStream send = response.send();
                WaitForResponse.ProvidingResponse entity = (WaitForResponse.ProvidingResponse) TransactionQueue.getQueue(resourceName).current();
                entity.request().getHeaders();
                entity.request().getPath();
                entity.request().getMethod();
                entity.request().getBody();
                assertThrows(UnsupportedOperationException.class, () -> entity.request().getFiles());
                assertThrows(UnsupportedOperationException.class, () -> entity.request().getMultiParts());
                assertThrows(UnsupportedOperationException.class, () -> entity.request().getFormParameters());
                assertThrows(UnsupportedOperationException.class, () -> entity.request().getUrlParameters());
                entity.request().getFullPath();

                send.write(returningBody.getBytes());
                send.flush();
                send.close();
            } catch (InputOutputError | IOException e) {
                fail(e);
            }
        });

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%s/", serverBuilder.getBinding().getDescription(), port)))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        final HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertEquals(response.headers().allValues("content-type"), List.of("text/plain"));
        assertEquals(returningBody, response.body());

        serverTrigger.abort();
    }

}