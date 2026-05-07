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
import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.rest.cache.CacheStrategy;
import com.webxells.dis.rest.cache.NoCache;
import com.webxells.dis.rest.cookie.CookiePersistenceStrategy;
import com.webxells.dis.rest.error.ErrorStrategy;
import com.webxells.dis.rest.error.Error;
import com.webxells.dis.rest.execution.HttpMethod;
import com.webxells.dis.rest.execution.HttpRedirect;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.execution.RestStrategy;
import com.webxells.dis.rest.execution.httpclient.HttpClientStrategy;
import com.webxells.dis.rest.url.ParameterParser;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Description("Holds information for rest operations")
public class RestConfig implements DisConfigApi {
    @Default("GET")
    private HttpMethod method = HttpMethod.GET;
    @Description("Url to be called")
    private String baseUrl;
    private Map<String, String> headers = new HashMap<>();
    @Description("Defines how to handle redirects")
    @Default("NORMAL")
    private HttpRedirect redirectStrategy = HttpRedirect.NORMAL;
    @Description("Which http version to use")
    @Default("HTTP_1_1")
    private HttpVersion version = HttpVersion.HTTP_1_1;
    @Description("Contains authorization details")
    private Authorization authorization;
    private String body;
    @Description("Adds url query parameters to 'baseUrl'")
    private ParameterParser urlQueryParameters;
    @Description("Files map of file index (REST request) and system Data")
    private Map<String, BinaryData> fileList;
    @Default("false")
    private boolean forceMultiPartFileTransfer;
    @Description("Skip ssl verification")
    @Default("false")
    private boolean disableSslVerification;
    @Description("Sets static cookies")
    private Map<String, String> cookies;
    @Description("Handles persisting of cookies")
    private CookiePersistenceStrategy cookiePersistenceStrategy;
    @Description("Sets clients priority")
    @Default("16")
    private int priority = 16;
    @Default("5")
    private int timeout = 5;
    @Default("SECONDS")
    private ChronoUnit timeoutUnit = ChronoUnit.SECONDS;
    @Description("Defines behaviour when an erroneous status code is returned")
    @Default("com.webxells.dis.handler.rest.error.ERROR")
    private ErrorStrategy errorStrategy = new Error();
    @Description("Defines http client implementation")
    @Default("com.webxells.dis.handler.rest.execution.httpclient.HttpClientStrategy")
    private RestStrategy execution;
    @Description("Defines cache strategy")
    @Default("No cache")
    private CacheStrategy cacheStrategy;
    @Description("If no response is necessary - Rest will call request asynchronous in background. This option disables this")
    @Default("false - allow asynchronous calls if possible")
    private boolean forceSynchronousCall;
    @Description("Max asynchronous calls in parallel")
    @Default("5")
    private int maxAsyncCalls = 5;
    @Description("Wait timeout if max asynchronous calls is reached")
    @Default("300")
    private long  waitForLessAsyncCalls = 300;

    public boolean isDisableSslVerification() {
        return disableSslVerification;
    }

    public void setDisableSslVerification(final boolean disableSslVerification) {
        this.disableSslVerification = disableSslVerification;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public ParameterParser getUrlQueryParameters() {
        return urlQueryParameters;
    }

    public void setUrlQueryParameters(final ParameterParser urlQueryParameters) {
        this.urlQueryParameters = urlQueryParameters;
    }

    public void setMethod(final HttpMethod method) {
        this.method = Objects.requireNonNull(method);
    }

    public boolean isForceMultiPartFileTransfer() {
        return forceMultiPartFileTransfer;
    }

    public void setForceMultiPartFileTransfer(final boolean forceMultiPartFileTransfer) {
        this.forceMultiPartFileTransfer = forceMultiPartFileTransfer;
    }
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(final String url) {
        this.baseUrl = Objects.requireNonNull(url);
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void addHeader(final String name, final String value) {
        headers.put(name, value);
    }

    public void setHeaders(final Map<String, String> headers) {
        this.headers = Objects.requireNonNull(headers);
    }

    public HttpRedirect getRedirectStrategy() {
        return redirectStrategy;
    }

    public void setRedirectStrategy(final HttpRedirect redirectStrategy) {
        this.redirectStrategy = Objects.requireNonNull(redirectStrategy);
    }

    public HttpVersion getVersion() {
        return version;
    }

    public void setVersion(final HttpVersion version) {
        this.version = Objects.requireNonNull(version);
    }

    public Authorization getAuthorization() {
        return authorization;
    }

    public void setAuthorization(final Authorization authorization) {
        this.authorization = Objects.requireNonNull(authorization);
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }

    public Map<String, String> getCookies() {
        return cookies;
    }

    public void setCookies(final Map<String, String> cookies) {
        this.cookies = Objects.requireNonNull(cookies);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(final int priority) {
        this.priority = priority;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }

    public ChronoUnit getTimeoutUnit() {
        return timeoutUnit;
    }

    public void setTimeoutUnit(final ChronoUnit timeoutUnit) {
        this.timeoutUnit = Objects.requireNonNull(timeoutUnit);
    }

    public Map<String, BinaryData> getFileList() {
        return fileList;
    }

    public void setFileList(final Map<String, BinaryData> fileList) {
        this.fileList = fileList;
    }

    public CookiePersistenceStrategy getCookiePersistenceStrategy() {
        return cookiePersistenceStrategy;
    }

    public void setCookiePersistenceStrategy(final CookiePersistenceStrategy cookiePersistenceStrategy) {
        this.cookiePersistenceStrategy = cookiePersistenceStrategy;
    }

    public RestStrategy getExecution() {
        return Optional.ofNullable(execution)
                .orElseGet(HttpClientStrategy::new);
    }

    public void setExecution(final RestStrategy execution) {
        this.execution = execution;
    }

    public void copy(final RestConfig other) {
        method = other.method;
        version = other.version;
        priority = other.priority;
        timeout = other.timeout;
        timeoutUnit = other.timeoutUnit;
        fileList = null == other.fileList ? null : new HashMap<>(other.fileList);
        cookies = null == other.cookies ? null : new HashMap<>(other.cookies);
        headers = null == other.headers ? null : new HashMap<>(other.headers);
        body = other.body;
        authorization = other.authorization;
        redirectStrategy = other.redirectStrategy;
        baseUrl = other.baseUrl;
        urlQueryParameters = other.urlQueryParameters;
        cookiePersistenceStrategy =
                Optional.ofNullable(other.cookiePersistenceStrategy)
                        .map(CookiePersistenceStrategy::getCleanCopy)
                        .orElse(null);
        execution = other.execution;
        cacheStrategy = other.cacheStrategy;
    }

    public CacheStrategy getCacheStrategy() {
        return null == cacheStrategy ? NoCache.instance() : cacheStrategy;
    }

    public void setCacheStrategy(final CacheStrategy cacheStrategy) {
        this.cacheStrategy = cacheStrategy;
    }

    public boolean isForceSynchronousCall() {
        return forceSynchronousCall;
    }

    public void setForceSynchronousCall(final boolean forceSynchronousCall) {
        this.forceSynchronousCall = forceSynchronousCall;
    }

    public int getMaxAsyncCalls() {
        return maxAsyncCalls;
    }

    public void setMaxAsyncCalls(final int maxAsyncCalls) {
        this.maxAsyncCalls = maxAsyncCalls;
    }

    public long getWaitForLessAsyncCalls() {
        return waitForLessAsyncCalls;
    }

    public void setWaitForLessAsyncCalls(final long waitForLessAsyncCalls) {
        this.waitForLessAsyncCalls = waitForLessAsyncCalls;
    }
}