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
package com.webxells.dis.okhttp.rest.execution;

import com.webxells.dis.rest.cookie.DisJar;
import com.webxells.dis.rest.execution.BodyPublisherOfInputStream;
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.HttpRedirect;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.test.cases.MockServerTestCase;
import com.webxells.dis.test.cases.mock.MockResponse;
import com.webxells.dis.test.cases.mock.RecordedRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class OkHttpTest extends MockServerTestCase {

    @Test
    void testAsyncWithRestError() throws URISyntaxException, InterruptedException{
        URI uri = new URI(String.format("http://invalid-host:666/%s/%s", random(), random()));

        OkHttpStrategy strategy = new OkHttpStrategy();
        OkHttpClient client = strategy.newClient()
                .version(HttpVersion.HTTP_1_1)
                .build();
        OkHttpRequest request = client.newRequest()
                .method(HttpMethod.GET.name())
                .uri(uri)
                .build();

        AtomicReference<String> errorMessageRef = new AtomicReference<>();
        AtomicInteger statusRef = new AtomicInteger();
        AtomicBoolean finalized = new AtomicBoolean();

        client.sendAsync(request)
                .error((a, b) -> {
                    statusRef.set(a);
                    errorMessageRef.set(b);
                })
                .ready(a -> fail())
                .finalize(response -> {
                    assertTrue(response.isEmpty());
                    finalized.set(true);
                });
        Thread.sleep(300);

        assertFalse(errorMessageRef.get().isEmpty());
        assertEquals(-1, statusRef.get());
        assertTrue(finalized.get());
    }

    @Test
    void testAsyncWithHttpError() throws URISyntaxException, InterruptedException, IOException {
        URI uri = new URI(String.format("http://localhost:9999/%s/%s", random(), random()));
        String errorMessage = random();

        OkHttpStrategy strategy = new OkHttpStrategy();
        OkHttpClient client = strategy.newClient()
                .version(HttpVersion.HTTP_1_1)
                .build();
        OkHttpRequest request = client.newRequest()
                .method(HttpMethod.GET.name())
                .uri(uri)
                .build();

        MockResponse mockResponse = new MockResponse();
        mockResponse
                .setBody(errorMessage)
                .setResponseCode(500);
        enqueue(mockResponse);

        AtomicReference<String> errorMessageRef = new AtomicReference<>();
        AtomicInteger statusRef = new AtomicInteger();
        AtomicBoolean finalized = new AtomicBoolean();

        client.sendAsync(request)
                .error((a, b) -> {
                    statusRef.set(a);
                    errorMessageRef.set(b);
                })
                .ready(a -> fail())
                .finalize(response -> {
                    assertTrue(response.isPresent());
                    assertEquals(500, response.get().getStatusCode());
                    finalized.set(true);
                });
        Thread.sleep(300);

        assertEquals(errorMessage, errorMessageRef.get());
        assertEquals(500, statusRef.get());
        assertTrue(finalized.get());

        RecordedRequest recordedRequest = takeRequest();
        assertEquals(uri.toString(), recordedRequest.getUrl());
    }

    @Test
    void testAsync() throws URISyntaxException, InterruptedException, IOException {
        String requestBody = random();
        String responseBody = random();
        String requestHeader = random();
        String requestValue1 = random();
        String requestValue2 = random();
        String responseHeader = random();
        String responseValue1 = random();
        String responseValue2 = random();
        URI uri = new URI(String.format("http://localhost:9999/%s/%s", random(), random()));

        OkHttpStrategy strategy = new OkHttpStrategy();
        OkHttpClient client = strategy.newClient()
                .version(HttpVersion.HTTP_1_1)
                .followRedirects(HttpRedirect.NEVER)
                .build();
        OkHttpRequest request = client.newRequest()
                .method(HttpMethod.POST.name())
                .uri(uri)
                .setBodyPublisher(BodyPublisherOfInputStream.byString(requestBody))
                .setHeader(requestHeader, requestValue1)
                .header(requestHeader, requestValue2)
                .build();

        final Request copy = request.copy(BodyPublisherOfInputStream.byString("other"));
        assertEquals("other", new String(copy.getBodyPublisher().get().readAllBytes()));

        MockResponse mockResponse = new MockResponse();
        mockResponse
                .addHeader(responseHeader, responseValue1)
                .addHeader(responseHeader, responseValue2)
                .setBody(responseBody)
                .setResponseCode(301);
        enqueue(mockResponse);

        AtomicReference<Response> responseRef = new AtomicReference<>();
        AtomicBoolean finalized = new AtomicBoolean();

        client.sendAsync(request)
                        .error((a, b) -> fail())
                        .ready(responseRef::set)
                        .finalize(a -> {
                            assertTrue(a.isPresent());
                            assertSame(a.get(), responseRef.get());
                            finalized.set(true);
                        });
        Thread.sleep(300);

        assertTrue(finalized.get());
        Response response = responseRef.get();

        assertEquals(301, response.getStatusCode());
        assertEquals(responseBody, new String(response.body().readAllBytes()));
        response.body().reset();
        assertEquals(responseBody, response.getFullBody().get());
        assertEquals(responseValue1, response.getHeaders().firstValue(responseHeader).get());
        assertEquals(List.of(responseValue1, responseValue2), response.getHeaders().get(responseHeader));

        RecordedRequest recordedRequest = takeRequest();
        assertEquals(uri.toString(), recordedRequest.getUrl());
        assertEquals(requestBody, recordedRequest.getBody().trim());
        assertEquals(List.of(requestValue1, requestValue2), recordedRequest.getHeaders(requestHeader));
    }

    @Test
    void simpleCall() throws IOException, URISyntaxException {
        String requestBody = random();
        String responseBody = random();
        String requestHeader = random();
        String requestValue1 = random();
        String requestValue2 = random();
        String responseHeader = random();
        String responseValue1 = random();
        String responseValue2 = random();
        URI uri = new URI(String.format("http://localhost:9999/%s/%s", random(), random()));

        OkHttpStrategy strategy = new OkHttpStrategy();
        OkHttpClient client = strategy.newClient()
                .version(HttpVersion.HTTP_1_1)
                .followRedirects(HttpRedirect.NEVER)
                .build();
        OkHttpRequest request = client.newRequest()
                .method(HttpMethod.POST.name())
                .uri(uri)
                .setBodyPublisher(BodyPublisherOfInputStream.byString(requestBody))
                .setHeader(requestHeader, requestValue1)
                .header(requestHeader, requestValue2)
                .build();

        final Request copy = request.copy(BodyPublisherOfInputStream.byString("other"));
        assertEquals("other", new String(copy.getBodyPublisher().get().readAllBytes()));

        MockResponse mockResponse = new MockResponse();
        mockResponse
                .addHeader(responseHeader, responseValue1)
                .addHeader(responseHeader, responseValue2)
                .setBody(responseBody)
                .setResponseCode(301);
        enqueue(mockResponse);

        Response response = client.send(request);

        assertEquals(301, response.getStatusCode());
        assertEquals(responseBody, new String(response.body().readAllBytes()));
        response.body().reset();
        assertEquals(responseBody, response.getFullBody().get());
        assertEquals(responseValue1, response.getHeaders().firstValue(responseHeader).get());
        assertEquals(List.of(responseValue1, responseValue2), response.getHeaders().get(responseHeader));

        RecordedRequest recordedRequest = takeRequest();
        assertEquals(uri.toString(), recordedRequest.getUrl());
        assertEquals(requestBody, recordedRequest.getBody().trim());
        assertEquals(List.of(requestValue1, requestValue2), recordedRequest.getHeaders(requestHeader));
    }

    @Test
    void cookieCall() throws IOException, InterruptedException, URISyntaxException {
        String cookie1Name = "a";
        String cookie1Value1 = random();
        String cookie1Value2 = random();
        String cookie2Name = "b";
        String cookie2Value1 = random();

        OkHttpStrategy strategy = new OkHttpStrategy();
        Client client = strategy.newClient()
                .version(HttpVersion.HTTP_1_1)
                .followRedirects(HttpRedirect.NEVER)
                .cookieJar(new DisJar())
                .build();
        Request request1 = client.newRequest()
                .method(HttpMethod.GET)
                .uri(new URI("http://localhost:9999/test"))
                .build();

        MockResponse mockResponse1 = new MockResponse();
        mockResponse1
                .addHeader("Set-Cookie", String.format("%s=%s;httpOnly;someShit",cookie1Name, cookie1Value1))
                .addHeader("Set-Cookie", String.format("%s=%s;httpOnly;someShit",cookie1Name, cookie1Value2))
                .addHeader("Set-Cookie", String.format("%s=%s;httpOnly;someShit",cookie2Name, cookie2Value1))
                .setResponseCode(200);
        enqueue(mockResponse1);


        assertEquals(200, client.send(request1).getStatusCode());
        takeRequest();

        Request request2 = client.newRequest()
                .method(HttpMethod.GET)
                .uri(new URI("http://localhost:9999/check"))
                .build();

        MockResponse mockResponse2 = new MockResponse();
        mockResponse2
                .setResponseCode(200);
        enqueue(mockResponse2);

        assertEquals(200, client.send(request2).getStatusCode());

        RecordedRequest recordedRequest = takeRequest();

        assertEquals(String.format("%s=%s; %s=%s", cookie1Name, cookie1Value2, cookie2Name, cookie2Value1),
                recordedRequest.getHeader("Cookie"));


    }

}