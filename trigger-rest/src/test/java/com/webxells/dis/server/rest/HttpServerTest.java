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
package com.webxells.dis.server.rest;

import com.webxells.dis.server.rest.binding.Localhost;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpServerTest extends SimpleTestCase {
    @Test
    void testFlow() throws InterruptedException {
        TestServerBuilder serverBuilder = new TestServerBuilder();
        AtomicInteger triggerCounter = new AtomicInteger();
        Localhost binding = new Localhost();
        binding.setFormat(Localhost.Format.IPV4_LOOPBACK);
        assertEquals("127.0.0.1", binding.getDescription());
        Listener listener = new Listener();
        listener.port = random(1);
        listener.bindAddress = binding;
        HttpServerConfig config = new HttpServerConfig();
        config.setWaitBetweenCheck(0);
        config.setListener(listener);
        config.setMaxQueueSize(5);
        config.setName(random());
        config.setServerEngine(serverBuilder);

        HttpServer serverTrigger = new HttpServer(config);
        assertFalse(serverTrigger.isRunning());

        serverTrigger.awaitAction(() -> {
            TransactionQueue.getQueue(config.getName()).next();
            triggerCounter.incrementAndGet();
        });
        Thread.sleep(600);
        assertTrue(serverTrigger.isRunning());
        assertEquals(0, triggerCounter.get());
        int expectedCalls = randomMax(35);
        final TestServerBuilder.TestServer server = serverBuilder.build();
        for (int i = expectedCalls; i-- > 0;) {
            server.triggerRequest();
        }
        Thread.sleep(1100);
        assertEquals(expectedCalls, triggerCounter.get());

        assertTrue(serverTrigger.isRunning());
        serverTrigger.abort();
        assertFalse(serverTrigger.isRunning());
    }


}