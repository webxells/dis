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
package com.webxells.dis.test.cases;

import com.webxells.dis.test.cases.websocket.WebSocketClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;

public class WebSocketServerTestCase extends SimpleTestCase {
    private static final Base64.Encoder BASE_ENCODER = Base64.getEncoder();
    private static final MessageDigest SHA1;
    private static final String ACCEPT_MAGIC = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";

    static {
        try {
            SHA1 = MessageDigest.getInstance("SHA-1");
        } catch (final NoSuchAlgorithmException e) {
            throw new AssertionFailedError("no sha1 hasher found", e);
        }
    }

    private static ServerSocket server;
    private static Thread serverThread;
    private static final Map<Integer, WebSocketClient> CLIENTS =  new ConcurrentHashMap<>();

    @BeforeAll
    static void setUpWebSocket() throws IOException {
        server = new ServerSocket(9666);
        serverThread = new Thread(() -> {
            while (server.isBound()) {
                try {
                    final WebSocketClient client = new WebSocketClient(server.accept());
                    handshakeClient(client);
                    watchClient(client);
                } catch (final SocketException e) {
                    if (!e.getMessage().endsWith("closed")) {
                        throw new RuntimeException("socket error", e);
                    }
                } catch (final IOException | NoSuchAlgorithmException e) {
                    throw new RuntimeException("server error", e);
                }
            }
        });
        serverThread.start();
    }

    @AfterAll
    static void tearDownWebSocket() throws IOException {
        clearTestSpecific();
        server.close();
        if (serverThread.isAlive()) {
            serverThread.interrupt();
        }
    }

    @BeforeEach
    protected void clear() throws IOException {
        clearTestSpecific();
    }

    protected String getUri() {
        return "ws://127.0.0.1:9666";
    }

    protected int currentConnectedSockets() {
        return CLIENTS.size();
    }

    protected void enqueue(final int index, final String response) throws IOException {
        CLIENTS.get(index)
                .getMessageTransition()
                    .newResponse(response);
    }

    protected void sendText(final int index, final String text) {
        CLIENTS.get(index)
                .getMessageTransition()
                    .newCommand(text);
    }

    protected List<String> getRequests(final int index) {
        return CLIENTS.get(index)
                .getMessageTransition()
                    .getLoggedRequests();
    }

    private static void clearTestSpecific() throws IOException {
        for (final WebSocketClient client : CLIENTS.values()) {
            client.close();
        }
        CLIENTS.clear();
    }

    private static void watchClient(final WebSocketClient client) throws IOException {
        CLIENTS.put(CLIENTS.size(), client);
        client.startWatchInput();
    }

    private static void handshakeClient(final WebSocketClient client) throws IOException, NoSuchAlgorithmException {
        assertValidFirstLine(client.readLine());
        final String clientHash = parseHeadersForClientHash(client);
        client.sendRawText(String.format("""
        HTTP/1.1 101 Switching Protocols
        Connection: Upgrade
        Upgrade: websocket
        Sec-WebSocket-Accept: %s
        
        """, createServerKey(clientHash.concat(ACCEPT_MAGIC))));
    }

    private static String parseHeadersForClientHash(final WebSocketClient client) throws IOException {
        String data;
        String result = null;
        do {
            data = client.readLine();
            if (null != data && data.startsWith("Sec-WebSocket-Key:")) {
                result = data.substring(data.indexOf(":") + 1).trim();
            }
        } while (null != data && !data.isBlank());
        if (null == result) {
            throw new AssertionFailedError("Could not find clientHash");
        }
        return result;
    }

    private static void assertValidFirstLine(final String data) {
        if (!data.startsWith("GET")) {
            throw new AssertionFailedError("No GET request");
        }
    }

    private static String createServerKey(final String string) {
        return BASE_ENCODER.encodeToString(SHA1.digest(string.getBytes(StandardCharsets.UTF_8)));
    }
}