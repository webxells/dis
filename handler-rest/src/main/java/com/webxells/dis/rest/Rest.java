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
package com.webxells.dis.rest;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.resource.SizeProvidingResource;
import com.webxells.dis.base.binary.RawData;
import com.webxells.dis.rest.cache.CacheEntity;
import com.webxells.dis.rest.content.ContentStrategy;
import com.webxells.dis.rest.cookie.CookiePersistenceStrategy;
import com.webxells.dis.rest.cookie.Disabled;
import com.webxells.dis.rest.cookie.PerCall;
import com.webxells.dis.rest.execution.BodyPublisher;
import com.webxells.dis.rest.execution.BodyPublisherOfInputStream;
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.Headers;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.rest.execution.RestStrategy;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class Rest implements SizeProvidingResource {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Rest.class); //@hint: have to be above user agent
    public static final String DEFAULT_USER_AGENT = createUserAgentString();

    protected RestConfig configuration;
    protected List<ContentStrategy> requestContentStrategy;
    protected ContentStrategy responseContentStrategy;
    protected Client client;
    protected final Map<String, String> defaultHeaders = new HashMap<>();

    private long size = -1;

    private static String createUserAgentString() {
        return Optional.ofNullable(ClassLoader.getSystemClassLoader().getResource("user-agent"))
                .map(Rest::getUserAgentByURL)
                .filter(Rest::validUserAgent)
                .orElse("DIS::Resource::Rest");
    }

    private static boolean validUserAgent(final String userAgent) {
        if (userAgent.isBlank()) {
            return false;
        }
        LOGGER.trace("Found resource user string: ".concat(userAgent));
        return true;
    }

    private static String getUserAgentByURL(final URL resource) {
        try(final InputStream input = resource.openStream()) {
            return new String(input.readAllBytes());
        } catch (final IOException ignored) { }
        return null;
    }

    public long getSize() {
        return size;
    }

    private void grabContentLength(final Headers headers) {
        size = headers.firstValue("Content-Length")
                .filter(a -> !a.isBlank())
                .filter(a -> a.chars().allMatch(Character::isDigit))
                .map(Long::parseLong)
                .orElse(-1L);
    }

    public ContentStrategy getResponseContentStrategy() {
        return responseContentStrategy;
    }

    public void setResponseContentStrategy(final ContentStrategy responseContentStrategy) {
        this.responseContentStrategy = responseContentStrategy;
    }

    public void setRequestContentStrategy(final List<ContentStrategy> requestContentStrategy) {
        this.requestContentStrategy = requestContentStrategy;
    }

    public List<ContentStrategy> getRequestContentStrategy() {
        return requestContentStrategy;
    }

    public RestConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(final RestConfig configuration) {
        this.configuration = configuration;
    }

    protected Response sendWithClient()
            throws IOException, InterruptedException {
        final boolean mayBeAsynchronous = null == responseContentStrategy && !configuration.isForceSynchronousCall();
        final Request request = buildRequest();
        if (mayBeAsynchronous) {
            callAsynchronous(request);
            return null;
        }
        return callCachableResponse(request);
    }

    private void callAsynchronous(final Request request) throws IOException, InterruptedException {
        configuration.getErrorStrategy().executeAsyncRest(getClient(), request);
    }

    private Response callCachableResponse(final Request request)
            throws IOException, InterruptedException {
        final CacheEntity cache = configuration.getCacheStrategy().request(request);
        if (cache.requiresRevalidation()) {
            LOGGER.debug(String.format("Querying (%s) %s", request.getMethod(), request.getUri()));
            final Response response = configuration.getErrorStrategy().executeRest(getClient(), cache.request());
            grabContentLength(response.getHeaders());
            cache.set(response);
        } else {
            LOGGER.debug("Reading from cache: (%s) %s", request.getMethod(), request.getUri());
        }
        return cache.response();
    }

    protected InputStream receiveWithClient() throws IOException, InterruptedException {
        final Response response = sendWithClient();
        return Optional.ofNullable(responseContentStrategy)
                .map(a -> a.parseOutputRequest(response))
                .orElse(new ByteArrayInputStream(new byte[0]));
    }


    protected void handleBody(final Request.Builder builder) {
        builder.method(configuration.getMethod());
        Optional.ofNullable(configuration.getBody())
                        .ifPresent(a -> builder.setBodyPublisher(BodyPublisherOfInputStream.byString(a)));

    }

    protected Client buildClient() {
        RestStrategy.Builder builder = configuration.getExecution().newClient()
                .followRedirects(configuration.getRedirectStrategy())
                .connectTimeout(configuration.getTimeout(), configuration.getTimeoutUnit());
        setCookiePersistence(builder);
        handleVersion(builder);
        if (configuration.isDisableSslVerification()) {
            builder.disableSslVerification();
        }
        return builder.build();
    }

    protected void setCookiePersistence(final RestStrategy.Builder builder) {
        final CookiePersistenceStrategy cookieStrategy = configuration.getCookiePersistenceStrategy();
        if (cookieStrategy instanceof Disabled) {
            return;
        }
        builder.cookieJar(Objects.requireNonNullElseGet(cookieStrategy, PerCall::new)
                .getJar(configuration.getCookies()));
    }


    protected Client getClient() {
        if (null == client) {
            Objects.requireNonNull(configuration);
            client = buildClient();
            handleHeaders();
        }
        return client;
    }

    protected Request buildRequest() {
        final Request.Builder builder = getClient().newRequest();
        builder.asyncTimer(configuration.getMaxAsyncCalls(), configuration.getWaitForLessAsyncCalls());
        parseUri().ifPresent(builder::uri);
        defaultHeaders.forEach(builder::header);
        builder.timeout(configuration.getTimeout(), configuration.getTimeoutUnit());
        if (!defaultHeaders.containsKey("USER-AGENT")) {
            builder.header("User-Agent", DEFAULT_USER_AGENT);
        }
        handleBody(builder);
        if (null != requestContentStrategy) {
            requestContentStrategy.forEach(
                    a -> a.parseInputRequest(configuration.getMethod(), builder, configuration));
        }
        handleFiles(builder);
        return builder.build();
    }

    protected Optional<URI> parseUri() {
        String url = configuration.getBaseUrl();
        if (null == url) {
            return Optional.empty();
        }
        if (null != configuration.getUrlQueryParameters()) {
            return Optional.of(configuration.getUrlQueryParameters().parseToUri(url));
        }
        return Optional.of(URI.create(url));
    }

    protected void handleFiles(final Request.Builder builder) {
        Optional.ofNullable(configuration.getFileList())
                .ifPresent(a -> addFilesToRequest(a, builder));
    }

    protected void addFilesToRequest(final Map<String, BinaryData> files, final Request.Builder builder) {
        final Request oldRequest = builder.build();
        Optional<BinaryData> originalRequestStream = createByHttpRequest(oldRequest);
        if (files.size() > 1 || originalRequestStream.isPresent() || configuration.isForceMultiPartFileTransfer()) {
            final MultiPartInputStream multiPartInputStream = new MultiPartInputStream(createBoundary());
            originalRequestStream
                    .ifPresent(b -> multiPartInputStream.addData(
                            oldRequest.getHeaders().firstValue("Content-Name").orElse("main"), b));
            files.forEach(multiPartInputStream::addData);
            changeToMultiPartRequest(builder, multiPartInputStream);
        } else {
            changeToSingleFileRequest(builder, files);
        }
    }

    private void changeToSingleFileRequest(final Request.Builder builder, final Map<String, BinaryData> files) {
        final String[] key = files.keySet().toArray(new String[1]);
        final BinaryData data = files.get(key[0]);
        builder.setHeader("Content-Type", Objects.requireNonNull(data.getMimeType()));
        builder.setHeader("Content-Name", key[0]);
        builder.setBodyPublisher(new BodyPublisherOfInputStream(() -> {
            try {
                return data.getContent();
            } catch (final IOException e) {
                throw new RuntimeException("Could not read files", e);
            }
        }, data.getSize()));
    }

    private void changeToMultiPartRequest(final Request.Builder builder, final MultiPartInputStream multiPartInputStream) {
        builder.setHeader("Content-Type", String.format("multipart/form-data;boundary=%s",
                multiPartInputStream.createSeparator().substring(2)));
        builder.setBodyPublisher(new MultiPartPublisher(multiPartInputStream));
    }

    private Optional<BinaryData> createByHttpRequest(final Request oldRequest) {
        return Optional.ofNullable(oldRequest.getBodyPublisher())
                .map(BodyPublisher::get)
                .map(inputStream -> createRawData(inputStream, oldRequest.getHeaders()));
    }

    private RawData createRawData(final InputStream inputStream, final Headers headers) {
        final byte[] content;
        try {
            content = inputStream.readAllBytes();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
        final RawData result = new RawData(new ByteArrayInputStream(content));
        result.setSize(content.length);
        result.setMimeType(headers.firstValue("Content-Type").orElse("text/plain"));
        return result;
    }

    private String createBoundary() {
        try {
            final MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(String.format("%s-%s", System.currentTimeMillis(),
                    Math.round(Math.random() * Long.MAX_VALUE)).getBytes());
            return new BigInteger(1, md5.digest()).toString(16);
        } catch (final NoSuchAlgorithmException e) {
            return Long.toHexString(Math.round(Math.random() * Long.MAX_VALUE));
        }
    }

    private void handleVersion(final RestStrategy.Builder builder) {
        builder.version(configuration.getVersion());
        if (HttpVersion.HTTP_2 == configuration.getVersion()) {
            builder.priority(configuration.getPriority());
        }
    }

    private void handleHeaders() {
        configuration.getHeaders().forEach((a,b) -> defaultHeaders.put(a.toUpperCase(), b));
        if (null != configuration.getAuthorization()) {
            handleAuthorization(configuration.getAuthorization());
        }
    }

    private void handleAuthorization(final Authorization authorization) {
        if (Authorization.Type.BASIC == authorization.getType()) {
            defaultHeaders.put("Authorization",
                    "Basic ".concat(new String(Base64.getEncoder().encode(String.format("%s:%s",
                    authorization.getUser(), authorization.getPassword()).getBytes()))));
        } else {
            throw new UnsupportedOperationException("Only capable of http basic authorization");
        }
    }
}