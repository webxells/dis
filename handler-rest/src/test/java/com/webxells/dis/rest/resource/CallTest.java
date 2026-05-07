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
package com.webxells.dis.rest.resource;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.rest.Authorization;
import com.webxells.dis.rest.Rest;
import com.webxells.dis.rest.RestConfig;
import com.webxells.dis.rest.cache.FileCache;
import com.webxells.dis.rest.cache.FileDateCacheCycle;
import com.webxells.dis.rest.content.Body;
import com.webxells.dis.rest.content.DatasetToBody;
import com.webxells.dis.rest.cookie.Disabled;
import com.webxells.dis.rest.error.Error;
import com.webxells.dis.rest.error.Retry;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.HttpRedirect;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.url.StandardUriQuery;
import com.webxells.dis.hash.engine.Murmur3;
import com.webxells.dis.plain.output.EchoConfig;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import com.webxells.dis.test.cases.FileTestCase;
import com.webxells.dis.test.cases.MockServerTestCase;
import com.webxells.dis.test.cases.mock.MockResponse;
import com.webxells.dis.test.cases.mock.RecordedRequest;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CallTest extends MockServerTestCase {
    private static FileTestCase fileTestCase;

    @AfterEach
    void clean() throws IOException {
        if (null != fileTestCase) {
            fileTestCase.cleanTestDir();
            fileTestCase = null;
        }
    }

    @Test
    void callAsync() throws InputOutputError, IOException, InterruptedException {
        sendAsync(200, null);
        sendAsync(201, random());
    }

    @Test
    void callAsyncError() throws InputOutputError, IOException, InterruptedException {
        sendAsync(500, random());
        getLogger(Error.class).assertEventWasFired(Logger.LogLevel.ERROR, "Error executing rest request (500): ");
    }

    @Test
    void callAsyncRetry() throws InputOutputError, IOException, InterruptedException {
        String param = random();
        String path = random();
        String reference = random();
        String requestHeaderParameter = random();
        String url = getUrl(random(), random());

        RestConfig config = new RestConfig();
        config.addHeader("Parameter", requestHeaderParameter);
        config.setMethod(HttpMethod.POST);
        config.setRedirectStrategy(HttpRedirect.NORMAL);
        config.setTimeoutUnit(ChronoUnit.HOURS);
        config.setTimeout(123);
        config.setCookiePersistenceStrategy(new Disabled());
        config.setPriority(23);
        config.setDisableSslVerification(true);
        config.setErrorStrategy(new Retry() {{
            setAmount(4);
            setTimeoutInMilliseconds(300);
        }});

        config.setBaseUrl(url);

        RestConfig copy = new RestConfig();
        copy.copy(config);
        assertEquals(url, copy.getBaseUrl());

        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setRequestContentStrategy(List.of(new DatasetToBody() {{
            this.setContent(newConfiguration()
                    .addPart(ConfigurationBuilder.newPart()
                            .setInput(createMappingPoint(reference, path))
                            .withContent(param))
                    .build());
            setAliasFilter(Map.of("param", new SimpleMappingPortrayal(reference, path)));
            setOutputConfig(new EchoConfig() {{
                setTemplate("test test test $param");
                setMultiMapping(Map.of());
            }});
        }}));

        MockResponse response1 = new MockResponse();
        response1
                .setResponseCode(500);
        MockResponse response2 = new MockResponse();
        response2
                .setResponseCode(500);
        MockResponse response3 = new MockResponse();
        response3
                .setResponseCode(200);
        enqueue(response1);
        enqueue(response2);
        enqueue(response3);

        int oldRequestCount = getRequestCount();

        long start = System.currentTimeMillis();
        try (InputStream receive = fixture.receive()) {
            Thread.sleep(1000);

            assertEquals(-1, receive.read());
        }

        assertEquals(oldRequestCount + 3, getRequestCount());

        for (RecordedRequest recordedRequest : List.of(takeRequest(), takeRequest(), takeRequest())) {
            assertEquals("test test test ".concat(param), recordedRequest.getBody());
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
        }
    }

    @Test
    void callAsyncHeavyLoad() throws InputOutputError, IOException, InterruptedException {
        String param = random();
        String path = random();
        String reference = random();
        String requestHeaderParameter = random();
        String url = getUrl(random(), random());

        RestConfig config = new RestConfig();
        config.addHeader("Parameter", requestHeaderParameter);
        config.setMethod(HttpMethod.POST);
        config.setRedirectStrategy(HttpRedirect.NORMAL);
        config.setTimeoutUnit(ChronoUnit.HOURS);
        config.setTimeout(600);
        config.setMaxAsyncCalls(2);
        config.setWaitForLessAsyncCalls(100);

        config.setBaseUrl(url);

        RestConfig copy = new RestConfig();
        copy.copy(config);
        assertEquals(url, copy.getBaseUrl());

        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setRequestContentStrategy(List.of(new DatasetToBody() {{
            this.setContent(newConfiguration()
                    .addPart(ConfigurationBuilder.newPart()
                            .setInput(createMappingPoint(reference, path))
                            .withContent(param))
                    .build());
            setAliasFilter(Map.of("param", new SimpleMappingPortrayal(reference, path)));
            setOutputConfig(new EchoConfig() {{
                setTemplate("test test test $param");
                setMultiMapping(Map.of());
            }});
        }}));

        enqueue(new MockResponse()
                .setMinWorkingTime(1500)
                .setResponseCode(200));
        enqueue(new MockResponse()
                .setMinWorkingTime(500)
                .setResponseCode(200));
        enqueue(new MockResponse()
                .setResponseCode(200));

        int oldRequestCount = getRequestCount();
        new Thread(() -> {
            try {
                assertEquals(-1, fixture.receive().read());
                assertEquals(-1, fixture.receive().read());
                assertEquals(-1, fixture.receive().read());
            } catch (final IOException | InputOutputError e) {
                throw new AssertionFailedError("failed", e);
            }
        }).start();

        assertEquals(oldRequestCount, getRequestCount());
        Thread.sleep(1100);
        assertEquals(oldRequestCount + 2, getRequestCount());
        Thread.sleep(700);
        assertEquals(oldRequestCount + 3, getRequestCount());

        for (RecordedRequest recordedRequest : List.of(takeRequest(), takeRequest(), takeRequest())) {
            assertEquals("test test test ".concat(param), recordedRequest.getBody());
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
        }
    }

    @Test
    void callWithFileCacheStrategy() throws InputOutputError, IOException, InterruptedException {
        fileTestCase = new FileTestCase();
        fileTestCase.setUpTestDir();
        String param = random();
        String responseBody1 = random();
        String responseBody2 = random();
        String responseBody = random();
        String requestHeaderParameter = random();
        String url = getUrl(random(), random());
        RestConfig config = new RestConfig();
        config.addHeader("Parameter", requestHeaderParameter);
        config.setMethod(HttpMethod.POST);
        config.setCacheStrategy(new FileCache() {{
            setEngine(new Murmur3());
            setCacheCycle(new FileDateCacheCycle() {{
                setAmount(3);
                setUnit(ChronoUnit.SECONDS);
            }});
            setCacheDir(fileTestCase.path());
        }});
        config.setBaseUrl(url);

        RestConfig copy = new RestConfig();
        copy.copy(config);
        assertEquals(url, copy.getBaseUrl());

        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setResponseContentStrategy(new Body());
        MockResponse response1 = new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody1);
        MockResponse response2 = new MockResponse()
                .setResponseCode(200)
                .setBody(responseBody2);

        enqueue(response1);
        enqueue(response2);

        int oldRequestCount = getRequestCount();

        try (InputStream receive = fixture.receive()) {
            assertEquals(responseBody1, new String(receive.readAllBytes()));
        }
        try (InputStream receive = fixture.receive()) {
            assertEquals(responseBody1, new String(receive.readAllBytes()));
        }
        Thread.sleep(3000);
        try (InputStream receive = fixture.receive()) {
            assertEquals(responseBody2, new String(receive.readAllBytes()));
        }
        try (InputStream receive = fixture.receive()) {
            assertEquals(responseBody2, new String(receive.readAllBytes()));
        }

        assertEquals(oldRequestCount + 2, getRequestCount());
    }

    @Test
    void callWithRetryErrorStrategy() throws InputOutputError, IOException, InterruptedException {
        String param = random();
        String path = random();
        String reference = random();
        String responseBody = random();
        String requestHeaderParameter = random();
        String url = getUrl(random(), random());

        RestConfig config = new RestConfig();
        config.addHeader("Parameter", requestHeaderParameter);
        config.setMethod(HttpMethod.POST);
        config.setRedirectStrategy(HttpRedirect.NORMAL);
        config.setTimeoutUnit(ChronoUnit.HOURS);
        config.setTimeout(123);
        config.setForceMultiPartFileTransfer(false);
        config.setCookiePersistenceStrategy(new Disabled());
        config.setPriority(23);
        config.setDisableSslVerification(true);
        config.setErrorStrategy(new Retry() {{
            setAmount(4);
            setTimeoutInMilliseconds(300);
        }});

        config.setBaseUrl(url);

        RestConfig copy = new RestConfig();
        copy.copy(config);
        assertEquals(url, copy.getBaseUrl());

        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setResponseContentStrategy(new Body());
        fixture.setRequestContentStrategy(List.of(new DatasetToBody() {{
            this.setContent(newConfiguration()
                    .addPart(ConfigurationBuilder.newPart()
                            .setInput(createMappingPoint(reference, path))
                            .withContent(param))
                    .build());
            setAliasFilter(Map.of("param", new SimpleMappingPortrayal(reference, path)));
            setOutputConfig(new EchoConfig() {{
                setTemplate("test test test $param");
                setMultiMapping(Map.of());
            }});
        }}));

        MockResponse response1 = new MockResponse();
        response1
                .setResponseCode(500);
        MockResponse response2 = new MockResponse();
        response2
                .setResponseCode(500);
        MockResponse response3 = new MockResponse();
        response3
                .setBody(responseBody)
                .setResponseCode(200);
        enqueue(response1);
        enqueue(response2);
        enqueue(response3);

        int oldRequestCount = getRequestCount();

        long start = System.currentTimeMillis();
        InputStream receive = fixture.receive();
        assertTrue((System.currentTimeMillis() - start) >= 600);

        InputStreamReader inputStreamReader = new InputStreamReader(receive);
        BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
        assertEquals(responseBody, bufferedRead.readLine());

        assertEquals(oldRequestCount + 3, getRequestCount());

        for (RecordedRequest recordedRequest : List.of(takeRequest(), takeRequest(), takeRequest())) {
            assertEquals("test test test ".concat(param), recordedRequest.getBody());
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
        }
    }

    @Test
    void receive() throws InputOutputError, IOException, InterruptedException {
        int returnCode = 200;
        String requestBodyPart1 = random();
        String requestBodyPart2 = random();
        String responseBody1 = random();
        String responseBody2 = random();
        String responseBody = String.format("%s%n%s", responseBody1, responseBody2);
        String requestHeaderParameter = random();
        String authUser = random();
        String authPassword = random();
        String url = getUrl(random(), random());
        String requestCookieParameter = random();

        String urlParameterName1 = random();
        String urlParameterValue1 = random();
        String urlParameterName2 = random();
        String urlParameterValue2 = random();
        RestConfig config = new RestConfig();
        config.setHeaders(Map.of("Parameter", requestHeaderParameter));
        config.setBody(requestBodyPart1.concat(requestBodyPart2));
        config.setAuthorization(new Authorization(authUser, authPassword));
        config.setCookies(Map.of("CookieParameter", requestCookieParameter));
        config.setMethod(HttpMethod.POST);
        config.setVersion(HttpVersion.HTTP_2);
        config.setUrlQueryParameters(new StandardUriQuery() {{
            setParameters(new LinkedHashMap<>() {{
                put(urlParameterName1, urlParameterValue1);
                put(urlParameterName2, urlParameterValue2);
            }});
        }});
        config.setBaseUrl(url);
        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setResponseContentStrategy(new Body());

        MockResponse response = new MockResponse();
        response
                .setBody(responseBody)
                .setResponseCode(returnCode);
        enqueue(response);

        InputStream receive = fixture.receive();
        InputStreamReader inputStreamReader = new InputStreamReader(receive);
        BufferedReader bufferedRead = new BufferedReader(inputStreamReader);
        String line1 = bufferedRead.readLine();
        String line2 = bufferedRead.readLine();
        String line3 = bufferedRead.readLine();
        String line4 = bufferedRead.readLine();

        RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
        assertEquals(requestBodyPart1.concat(requestBodyPart2), recordedRequest.getBody());
        assertEquals("CookieParameter=".concat(requestCookieParameter), recordedRequest.getHeader("Cookie"));
        assertEquals(requestHeaderParameter, recordedRequest.getHeader("Parameter"));
        assertEquals("POST", recordedRequest.getMethod());
        assertEquals(String.format("%s/?%s=%s&%s=%s", url, urlParameterName1, urlParameterValue1, urlParameterName2,
                urlParameterValue2),recordedRequest.getUrl());
        assertEquals(responseBody1, line1);
        assertEquals(responseBody2, line2);
        assertNull(line3);
        assertNull(line4);
    }

    @Test
    void sendWithFiles() throws InputOutputError, IOException {
        String name1 = random();
        String filename1 = random();
        String fileType1 = random();
        String content1 = random();
        BinaryData data1 = mock(BinaryData.class);
        ByteArrayInputStream part1 = new ByteArrayInputStream(content1.getBytes());
        when(data1.getContent()).thenReturn(part1);
        when(data1.getName()).thenReturn(filename1);
        when(data1.getMimeType()).thenReturn(fileType1);
        when(data1.getSize()).thenReturn(Long.valueOf(content1.length()));
        String name2 = random();
        String filename2 = random();
        String fileType2 = random();
        String content2 = random();
        BinaryData data2 = mock(BinaryData.class);
        ByteArrayInputStream part2 = new ByteArrayInputStream(content2.getBytes());
        when(data2.getContent()).thenReturn(part2);
        when(data2.getName()).thenReturn(filename2);
        when(data2.getMimeType()).thenReturn(fileType2);
        when(data2.getSize()).thenReturn(Long.valueOf(content2.length()));
        String requestBody = random();
        String url = getUrl(random(), random());

        RestConfig config = new RestConfig();
        config.setMethod(HttpMethod.POST);
        config.setBaseUrl(url);
        config.setForceSynchronousCall(true);
        Map<String, BinaryData> fileList = new LinkedHashMap<>() {{
            put(name1, data1);
            put(name2, data2);
        }};
        config.setFileList(fileList);

        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setRequestContentStrategy(List.of(new Body()));
        try (OutputStream send = fixture.send()) {
            enqueue(new MockResponse());
            send.write(requestBody.getBytes());
            send.flush();
            List<String> expected = new LinkedList<>();
            expected.add("--[a-z0-9]{16,32}");
            expected.add(Pattern.quote("Content-Disposition:form-data;name=\"main\""));
            expected.add(Pattern.quote("Content-Type:text/plain"));
            expected.add("");
            expected.add(Pattern.quote(requestBody));
            appendData(expected, name1, data1, content1);
            appendData(expected, name2, data2, content2);
            expected.add("--[a-z0-9]{16,32}--");
            RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
            assertLinesMatch(expected, recordedRequest.getBody().lines().collect(Collectors.toList()));
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
            assertLinesMatch(
                    List.of("multipart/form-data;boundary=[a-z0-9]{16,32}"),
                    List.of(recordedRequest.getHeader("Content-Type")));
        }
    }

    @Test
    void sendWithSingleFileShouldNotSendMultiPartRequest() throws InputOutputError, IOException,
            InterruptedException {
        String name = random();
        String filename = random();
        String fileType = random();
        String content = random();
        BinaryData data = mock(BinaryData.class);
        when(data.getContent()).thenReturn(new ByteArrayInputStream(content.getBytes()));
        when(data.getName()).thenReturn(filename);
        when(data.getMimeType()).thenReturn(fileType);
        when(data.getSize()).thenReturn((long) content.length());

        String requestBodyShouldBeIgnored = random();
        String url = getUrl(random(), random());

        RestConfig config = new RestConfig();
        config.setMethod(HttpMethod.POST);
        config.setBaseUrl(url);
        config.setForceSynchronousCall(true);
        config.setFileList(Collections.singletonMap(name, data));

        Call fixture = new Call();
        fixture.setConfiguration(config);
        try (OutputStream send = fixture.send()) {
            MockResponse response = new MockResponse();
            response.setResponseCode(200);
            enqueue(response);
            send.write(requestBodyShouldBeIgnored.getBytes());
            send.flush();
            RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
            assertEquals(0, fixture.getSize());
            assertEquals(content, recordedRequest.getBody());
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
            assertEquals(fileType, recordedRequest.getHeader("Content-Type"));
            assertEquals(name, recordedRequest.getHeader("Content-Name"));
        }
    }

    private void appendData(final List<String> expected, final String name, final BinaryData data, final String content) {
        expected.add("--[a-z0-9]{16,32}");
        expected.add(Pattern.quote(String.format("Content-Disposition:form-data;name=\"%s\";filename=\"%s\"", name,
                data.getName())));
        expected.add(Pattern.quote(String.format("Content-Type:%s", data.getMimeType())));
        expected.add("");
        expected.add(Pattern.quote(content));
    }

    @Test
    void sendErroneously() {
        Assertions.assertThrows(IOException.class, () -> send(500, random()));
    }

    @Test
    void sendSuccessfullyOtherUserAgent() throws InterruptedException, IOException, InputOutputError {
        send(200, random());
    }

    @Test
    void sendSuccessfully() throws InterruptedException, IOException, InputOutputError {
        send(200, null);
    }

    void send(int returnCode, final String userAgent) throws InputOutputError, IOException, InterruptedException {
        String requestBodyPart1 = random();
        String requestBodyPart2 = random();
        String requestHeaderParameter = random();
        String requestIgnoredBody = random();
        String authUser = random();
        String authPassword = random();
        String url = getUrl(random(), random());
        String requestCookieParameter = random();

        RestConfig config = new RestConfig();
        config.setHeaders(new HashMap<>(Map.of("Parameter", requestHeaderParameter)));
        if (null != userAgent) {
            config.getHeaders().put("user-agent", userAgent);
        }
        config.setBody(requestIgnoredBody);
        config.setAuthorization(new Authorization(authUser, authPassword));
        config.setCookies(Map.of("CookieParameter", requestCookieParameter));
        config.setMethod(HttpMethod.POST);
        config.setVersion(HttpVersion.HTTP_2);
        config.setForceSynchronousCall(true);
        config.setBaseUrl(url);
        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setRequestContentStrategy(List.of(new Body()));
        try (OutputStream send = fixture.send()) {
            MockResponse response = new MockResponse();
            response.setResponseCode(returnCode);
            enqueue(response);
            send.write(requestBodyPart1.getBytes());
            send.write(requestBodyPart2.getBytes());
            send.flush();
            RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
            assertEquals(0, fixture.getSize());
            assertEquals(requestBodyPart1.concat(requestBodyPart2), recordedRequest.getBody());
            assertEquals("CookieParameter=".concat(requestCookieParameter), recordedRequest.getHeader("Cookie"));
            assertEquals(requestHeaderParameter, recordedRequest.getHeader("Parameter"));
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
            assertEquals(null == userAgent ? Rest.DEFAULT_USER_AGENT : userAgent, recordedRequest.getHeader("User-Agent"));
        }

    }

    void sendAsync(int returnCode, final String userAgent) throws InputOutputError, IOException, InterruptedException {
        String requestBodyPart1 = random();
        String requestBodyPart2 = random();
        String requestHeaderParameter = random();
        String requestIgnoredBody = random();
        String authUser = random();
        String authPassword = random();
        String url = getUrl(random(), random());
        String requestCookieParameter = random();

        RestConfig config = new RestConfig();
        config.setHeaders(new HashMap<>(Map.of("Parameter", requestHeaderParameter)));
        if (null != userAgent) {
            config.getHeaders().put("user-agent", userAgent);
        }
        config.setBody(requestIgnoredBody);
        config.setAuthorization(new Authorization(authUser, authPassword));
        config.setCookies(Map.of("CookieParameter", requestCookieParameter));
        config.setMethod(HttpMethod.POST);
        config.setForceSynchronousCall(false);
        config.setBaseUrl(url);
        Call fixture = new Call();
        fixture.setConfiguration(config);
        fixture.setRequestContentStrategy(List.of(new Body()));
        try (OutputStream send = fixture.send()) {
            MockResponse response = new MockResponse();
            response.setResponseCode(returnCode);
            enqueue(response);
            send.write(requestBodyPart1.getBytes());
            send.write(requestBodyPart2.getBytes());
            send.flush();
            Thread.sleep(200);
            RecordedRequest recordedRequest = takeRequest(2, ChronoUnit.SECONDS);
            assertEquals(-1, fixture.getSize());
            assertEquals(requestBodyPart1.concat(requestBodyPart2), recordedRequest.getBody());
            assertEquals("CookieParameter=".concat(requestCookieParameter), recordedRequest.getHeader("Cookie"));
            assertEquals(requestHeaderParameter, recordedRequest.getHeader("Parameter"));
            assertEquals("POST", recordedRequest.getMethod());
            assertEquals(url, recordedRequest.getUrl());
            assertEquals(null == userAgent ? Rest.DEFAULT_USER_AGENT : userAgent, recordedRequest.getHeader("User-Agent"));
        }

    }


}