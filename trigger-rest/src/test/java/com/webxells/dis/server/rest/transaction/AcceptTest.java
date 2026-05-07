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

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.server.rest.HttpServer;
import com.webxells.dis.server.rest.HttpServerConfig;
import com.webxells.dis.server.rest.Listener;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.http.BuiltInServer;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AcceptTest extends SimpleTestCase {

    @Test
    void test() throws IOException, InterruptedException {
        String resourceName = random();
        TransactionQueue.getQueue(resourceName).reset();

        int port = 9666;
        Listener listener = new Listener();
        listener.port = port;
        Listener listener2 = new Listener();
        listener2.port = port;
        assertEquals(listener2, listener);

        BuiltInServer serverBuilder = new BuiltInServer()
                .setBindAddress(listener.bindAddress)
                .setPort(port);
        serverBuilder.setMaxQueueSize(4);
        serverBuilder.setWorkers(4);
        serverBuilder.setRootPath("/");
        serverBuilder.setSaveRequestToFile(false);
        serverBuilder.setRequestContentTmpDirectory(System.getProperty("java.io.tmpdir"));
        serverBuilder.setResponseHeaders(null);

        RestMapper restMapper = new RestMapper();
        restMapper.setMethod(Request.Method.POST);
        restMapper.setPath("/");

        final HttpServerConfig config = new HttpServerConfig();
        config.setWaitBetweenCheck(0);
        config.setListener(listener);
        config.setMaxQueueSize(5);
        config.setName(resourceName);
        config.setDisableQueue(false);
        config.setServerEngine(serverBuilder);
        config.setRestMapper(restMapper);

        HttpServer serverTrigger = new HttpServer(config);

        AtomicBoolean triggered = new AtomicBoolean();
        serverTrigger.awaitAction(() -> triggered.set(true));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://%s:%s/", serverBuilder.getBinding().getDescription(), port)))
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpClient httpClient = HttpClient.newBuilder()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        Thread.sleep(300);

        assertEquals(202, response.statusCode());
        assertEquals(List.of("text/plain"), response.headers().allValues("content-type"));

        assertTrue(triggered.get());

        serverTrigger.abort();
    }
}