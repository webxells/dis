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
package com.webxells.dis.server.rest.http;

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Request.Method;
import com.webxells.dis.server.rest.binding.Localhost;
import com.webxells.dis.server.rest.filter.auth.BearerToken;
import com.webxells.dis.server.rest.strategy.RestMapper;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaServerTest extends SimpleTestCase {

    private class ExpectedRequest {
        public Method method;
        public String path;
        public Map<String, String> headers;
        public String fullPath;
    }
    private final HttpClient httpClient = HttpClient.newBuilder()
            .build();

    private ExpectedRequest expectedRequest;

    @BeforeEach
    void setUp() {
        expectedRequest = new ExpectedRequest();
    }

    @Test
    void test() throws IOException, InterruptedException {
        RestMapper restMapper = new RestMapper();
        restMapper.setMethod(Method.POST);
        restMapper.setPath("/".concat(random()));
        BearerToken authorization = new BearerToken();
        String token = random();
        authorization.setToken(token);
        restMapper.setRequestFilters(List.of(authorization));
        BuiltInServer config = new BuiltInServer()
                .setPort(9667)
                .setBindAddress(new Localhost());

        JavaServer fixture = new JavaServer(config);
        assertFalse(fixture.isRunning());
        fixture.register(restMapper, request -> {
            try {
                assertValidRequest(request);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        assertTrue(fixture.isRunning());

        call(restMapper.getPath(), "Bearer ".concat(token), Map.of(random(), random(),
                random(), random()), new String[]{random(), random(),
                random(), random()}, 666);

        call(restMapper.getPath(), random(), Map.of(random(), random(),
                random(), random()), new String[]{random(), random(),
                random(), random()}, 401);

        call("/invalid", "Bearer ".concat(token), Map.of(random(), random(),
                random(), random()), new String[]{random(), random(),
                random(), random()}, 404);

        fixture.drop(restMapper);
        assertFalse(fixture.isRunning());
    }


    private void call(final String path, final String token, final Map<String, String> parameters, final String[] headers, int status) throws IOException, InterruptedException {
        expectedRequest.method = Method.POST;
        expectedRequest.path = path;
        expectedRequest.fullPath = String.format("%s?%s", path, parameters.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(a -> String.format("%s=%s", a.getKey(), a.getValue()))
                .collect(Collectors.joining("&")));
        Map<String, String> expectedHeaders = new HashMap<>();
        for (int i = 0; i < headers.length; i+=2) {
            expectedHeaders.put(headers[i], headers[i+1]);
        }
        expectedHeaders.put("Authorization", token);
        expectedRequest.headers = expectedHeaders;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(String.format("http://127.0.0.1:9667%s", expectedRequest.fullPath)))
                .method("POST", HttpRequest.BodyPublishers.ofString(String.valueOf(random(1L))))
                .headers(headers)
                .header("Authorization", token)
                .build();
        final HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
        assertEquals(status, response.statusCode());
    }

    private void assertValidRequest(final Request request) throws IOException {
        assertEquals(expectedRequest.path, request.getPath());
        assertEquals(expectedRequest.fullPath, request.getFullPath());
        assertSame(expectedRequest.method, request.getMethod());

        toListValue(expectedRequest.headers)
                .forEach((key, value) -> assertEquals(value, request.getHeaders().get(key.toLowerCase())));
        request.respond().send(666);
    }

    private Map<String, List<String>> toListValue(final Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, a -> List.of(a.getValue())));
    }

}