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
package com.webxells.dis.rest.execution.httpclient;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.rest.cookie.CookieJar;
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.HttpRedirect;
import com.webxells.dis.rest.execution.HttpVersion;
import com.webxells.dis.rest.execution.RestStrategy;
import com.webxells.dis.rest.execution.TrustAllManager;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

@Description("Uses native java http client")
public class HttpClientStrategy implements RestStrategy {
    public static class Builder implements RestStrategy.Builder {
        private final java.net.http.HttpClient.Builder
                clientBuilder = java.net.http.HttpClient.newBuilder();
        @Override
        public RestStrategy.Builder followRedirects(final HttpRedirect httpRedirect) {
            switch (httpRedirect) {
                case NEVER:
                    clientBuilder.followRedirects(Redirect.NEVER);
                    break;
                case ALWAYS:
                    clientBuilder.followRedirects(Redirect.ALWAYS);
                    break;
                case NORMAL:
                    clientBuilder.followRedirects(Redirect.NORMAL);
            }
            return this;
        }

        @Override
        public RestStrategy.Builder cookieJar(final CookieJar cookieJar) {
            clientBuilder.cookieHandler(new HttpDisJar(cookieJar));
            return this;
        }

        @Override
        public RestStrategy.Builder connectTimeout(final int timeout, final ChronoUnit unit) {
            clientBuilder.connectTimeout(Duration.of(timeout, unit));
            return this;
        }

        @Override
        public RestStrategy.Builder version(final HttpVersion httpVersion) {
            switch (httpVersion) {
                case HTTP_1_1:
                    clientBuilder.version(Version.HTTP_1_1);
                    break;
                case HTTP_2:
                    clientBuilder.version(Version.HTTP_2);
            }
            return this;
        }

        @Override
        public RestStrategy.Builder disableSslVerification() {
            try {
                final SSLContext context = SSLContext.getInstance("TLS");
                context.init(new KeyManager[0], new TrustManager[]{TrustAllManager.getInstance()}, new SecureRandom());
                clientBuilder.sslContext(context);
            } catch (final NoSuchAlgorithmException | KeyManagementException e) {
                throw new UnsupportedOperationException("Could not skip ssl validation", e);
            }
            return this;
        }

        @Override
        public RestStrategy.Builder priority(final int priority) {
            clientBuilder.priority(priority);
            return this;
        }

        @Override
        public Client build() {
            return new HttpClient(clientBuilder.build());
        }
    }

    @Override
    public Builder newClient() {
        return new Builder();
    }
}