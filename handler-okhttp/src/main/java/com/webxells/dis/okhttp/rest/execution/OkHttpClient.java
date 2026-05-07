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

import com.webxells.dis.api.Logger;
import com.webxells.dis.rest.cookie.CookieJar;
import com.webxells.dis.rest.execution.*;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Protocol;
import okhttp3.ResponseBody;

public class OkHttpClient implements Client {
    public static class OkHttpLimiter extends AsyncLimiter {
        protected OkHttpLimiter(final Request request) {
            super(request);
        }
    }
    public static class Builder implements RestStrategy.Builder {
        private final okhttp3.OkHttpClient.Builder okHttpBuilder = new okhttp3.OkHttpClient.Builder();
        @Override
        public Builder followRedirects(final HttpRedirect httpRedirect) {
            switch (httpRedirect) {
                case NEVER:
                    okHttpBuilder.followRedirects(false);
                    break;
                case ALWAYS:
                case NORMAL:
                    okHttpBuilder.followRedirects(true);
                    break;
            }
            return this;
        }
        @Override
        public Builder cookieJar(final CookieJar cookieHandler) {
            okHttpBuilder.cookieJar(new OkHttpCookieJar(cookieHandler));
            return this;
        }

        @Override
        public Builder connectTimeout(final int timeout, final ChronoUnit unit) {
            okHttpBuilder.connectTimeout(timeout, TimeUnit.of(unit));
            okHttpBuilder.callTimeout(timeout, TimeUnit.of(unit));
            return this;
        }

        @Override
        public Builder version(final HttpVersion httpVersion) {
            switch (httpVersion) {
                case HTTP_1_1:
                    okHttpBuilder.protocols(List.of(Protocol.HTTP_1_1));
                    break;
                case HTTP_2:
                    okHttpBuilder.protocols(List.of(Protocol.HTTP_2));
                    break;
            }
            return this;
        }

        @Override
        public Builder disableSslVerification() {
            try {
                final SSLContext context = SSLContext.getInstance("SSL");
                context.init(new KeyManager[]{}, new TrustManager[]{TrustAllManager.getInstance()}, new SecureRandom());
                okHttpBuilder.sslSocketFactory(context.getSocketFactory(), TrustAllManager.getInstance());
                okHttpBuilder.hostnameVerifier((a, b) -> true);
            } catch (final NoSuchAlgorithmException | KeyManagementException e) {
                throw new UnsupportedOperationException("Could not skip ssl validation", e);
            }
            return this;
        }

        @Override
        public Builder priority(final int priority) {
            LOGGER.debug("priority parameter is not supported");
            return this;
        }

        @Override
        public OkHttpClient build() {
            return new OkHttpClient(okHttpBuilder.build());
        }


    }
    private final static Logger LOGGER = LoggerProxyFactory.logger(OkHttpClient.class);

    private final okhttp3.OkHttpClient okHttpClient;

    static Response createResponse(final okhttp3.Response response, final URI uri) {
        return new Response(response.code(), new OkHttpHeaders(response), uri, getBodyStream(response));
    }

    private static InputStream getBodyStream(final okhttp3.Response response) {
        return Optional.ofNullable(response.body())
                .map(ResponseBody::byteStream)
                .map(a -> {
                    try {
                        return new ByteArrayInputStream(a.readAllBytes());
                    } catch (final IOException e) {
                        throw new RuntimeException("could not copy body", e);
                    }
                })
                .orElse(null);
    }

    private OkHttpClient(final okhttp3.OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
    }

    @Override
    public OkHttpRequest.Builder newRequest() {
        return new OkHttpRequest.Builder();
    }

    @Override
    public HttpVersion version() {
        return okHttpClient.protocols().contains(Protocol.HTTP_2) ? HttpVersion.HTTP_2 : HttpVersion.HTTP_1_1;
    }

    @Override
    public Response send(final Request request) throws IOException {
        assertValidRequest(request);
        try (final okhttp3.Response response =
                     okHttpClient.newCall(((OkHttpRequest) request).getRequest()).execute()) {
            return createResponse(response, request.getUri());
        }
    }

    @Override
    public ReturnPromise sendAsync(final Request request) {
        assertValidRequest(request);
        return new OkHttpLimiter(request).send(new HttpAsyncPromise() {
            @Override
            protected void sendAndFinishWithHandleResponse() throws Exception {
                final Call call = okHttpClient.newCall(((OkHttpRequest) request).getRequest());
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(final Call call, final IOException e) {
                        handleResponse(e);
                    }

                    @Override
                    public void onResponse(final Call call, final okhttp3.Response response) throws IOException {
                        handleResponse(OkHttpClient.createResponse(response, request.getUri()));
                    }
                });
            }
        });
    }

    private void assertValidRequest(final Request request) {
        if (!(request instanceof OkHttpRequest)) {
            throw new IllegalArgumentException("Invalid request used");
        }
    }
}