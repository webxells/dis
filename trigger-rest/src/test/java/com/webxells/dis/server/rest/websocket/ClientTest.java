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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.server.rest.TransactionQueue;
import com.webxells.dis.server.rest.websocket.resource.WebsocketReceiver;
import com.webxells.dis.server.rest.websocket.resource.WebsocketSender;
import com.webxells.dis.test.cases.WebSocketServerTestCase;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClientTest extends WebSocketServerTestCase {


    @Test
    void testWhole() throws IOException, InterruptedException, InputOutputError {
        String data = random();
        ClientConfig triggerConfig = new ClientConfig();
        triggerConfig.setDisableQueue(false);
        triggerConfig.setUri(getUri());
        triggerConfig.setName("test");
        triggerConfig.setTimeout(new ClientConfig.Timeout() {{
            amount = 10;
            unit = ChronoUnit.SECONDS;
        }});
        AtomicInteger counter = new AtomicInteger(0);
        List<String> expectedMessages = List.of(
                data,
                "GOT: " + data
        );
        WebsocketReceiver receiver = new WebsocketReceiver();
        receiver.setName("test");
        assertThrows(UnsupportedOperationException.class, receiver::send);
        WebsocketSender sender = new WebsocketSender();
        sender.setName("test");
        sender.setInstantFlush(false);
        sender.setSendingType(WebsocketSender.Type.TEXT);
        sender.setSendMessageMaxCapacity(3000);
        assertThrows(UnsupportedOperationException.class, sender::receive);
        Client trigger = new Client(triggerConfig);
        assertFalse(trigger.isRunning());
        assertEquals(0, currentConnectedSockets());
        trigger.awaitAction(() -> {
            try (InputStream input = receiver.receive()){
                assertEquals(expectedMessages.get(counter.getAndIncrement()),
                        new String(input.readAllBytes()));
            } catch (final IOException e) {
                throw new AssertionFailedError("could not receive", e);
            }
        });
        Thread.sleep(1500);
        assertTrue(trigger.isRunning());
        assertEquals(1, currentConnectedSockets());

        sendText(0, data);
        enqueue(0, "GOT: " + data);
        Thread.sleep(1000);
        try (OutputStream s = sender.send()) {
            s.write(data.getBytes(StandardCharsets.UTF_8));
            s.flush();
        }
        Thread.sleep(1000);
        assertEquals(2, counter.get());
    }

    @Test
    void testSimple() throws IOException, InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        ClientConfig config = new ClientConfig();
        config.setUri(getUri());
        config.setName("test");
        config.setTimeout(new ClientConfig.Timeout() {{
            amount = 10;
            unit = ChronoUnit.SECONDS;
        }});
        config.setHeaders(Map.of(random(), random()));
        Client fixture = new Client(config);
        assertFalse(fixture.isRunning());
        assertEquals(0, currentConnectedSockets());
        fixture.awaitAction(() -> {
            counter.incrementAndGet();
            TransactionQueue.getQueue(config.getName()).next();
        });
        Thread.sleep(1500);
        assertEquals(0, counter.get());
        assertEquals(1, currentConnectedSockets());
        sendText(0, random());
        Thread.sleep(1000);
        assertEquals(1, counter.get());
        assertTrue(fixture.isRunning());
        fixture.abort();
    }

}