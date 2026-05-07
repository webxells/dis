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

import com.webxells.dis.test.cases.mock.MockResponse;
import com.webxells.dis.test.cases.mock.RecordedRequest;
import com.webxells.dis.test.cases.server.MockWebServer;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;

public class MockServerTestCase extends SimpleTestCase {
    protected static MockWebServer server;

    @BeforeAll
    static void setUpMockServer() {
        server = new MockWebServer();
    }

    protected static void enqueue(final MockResponse response) {
        server.enqueue(response);
    }

    protected static RecordedRequest takeRequest() {
        return server.takeRequest();
    }

    protected static RecordedRequest takeRequest(final int delay, final ChronoUnit timeUnit) {
        return server.takeRequest(delay, timeUnit);
    }

    protected static int getRequestCount() {
        return server.getRequestCount();
    }

    @AfterAll
    static void shutDownMockServer() {
        if (null != server) {
            server.stop();
        }
    }

    @AfterEach
    synchronized void resetServer() {
        server.reset();
    }

    protected String getUrl(final String... path) {
        return getUrl(String.join("/", path));
    }

    protected String getUrl(final String path) {
        return server.getServerUrl(path);
    }

}