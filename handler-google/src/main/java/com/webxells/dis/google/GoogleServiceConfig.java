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
package com.webxells.dis.google;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

@Description("Specifies usage of the google service")
public class GoogleServiceConfig {
    @Required
    @Description("Google api key, to be allowed using this service")
    private String apiKey;
    @Description("Number of retries if an error occurred")
    @Default("2")
    private int maxRetries = 2;
    @Description("Sets the maximum number of queries that will be executed during a 1 second interval")
    @Default("1")
    private int queryRateLimit = 1;
    @Description("Time limit to retry errors; setting to 0 will retry requests forever")
    @Default("800")
    private int retryTimeout = 800;
    @Description("Timeout for the initial connection; 0 means no timeout")
    @Default("800")
    private int connectTimeout = 800;
    @Description("Time to wait for getting data from the server; 0 means no timeout")
    @Default("800")
    private int readTimeout = 800;
    @Description("Time until when something has to be written to the server; 0 means no timeout")
    @Default("800")
    private int writeTimeout = 800;

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(final String apiKey) {
        this.apiKey = apiKey;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(final int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public int getQueryRateLimit() {
        return queryRateLimit;
    }

    public void setQueryRateLimit(final int queryRateLimit) {
        this.queryRateLimit = queryRateLimit;
    }

    public int getRetryTimeout() {
        return retryTimeout;
    }

    public void setRetryTimeout(final int retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(final int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(final int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getWriteTimeout() {
        return writeTimeout;
    }

    public void setWriteTimeout(final int writeTimeout) {
        this.writeTimeout = writeTimeout;
    }
}
