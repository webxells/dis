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
package com.webxells.dis.rest.execution;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class HttpAsyncPromise implements ReturnPromise {
    public static final Consumer<Response> EMPTY_SUCCESS = a -> {};
    public static final BiConsumer<Integer, String> EMPTY_ERROR = (a, b) -> {};
    public static final Consumer<Optional<Response>> EMPTY_COMPLETION = a -> {};

    private static final Logger LOGGER = LoggerProxyFactory.logger(HttpAsyncPromise.class);

    private Throwable caughtError;

    private Consumer<Response> onSuccess = EMPTY_SUCCESS;
    private BiConsumer<Integer, String> onError = EMPTY_ERROR;
    private Consumer<Optional<Response>> afterCompletion = EMPTY_COMPLETION;

    public HttpAsyncPromise() {
        try {
            sendAndFinishWithHandleResponse();
        } catch (final Throwable e) {
            caughtError = e;
        }
    }

    protected abstract void sendAndFinishWithHandleResponse() throws Exception;

    @Override
    public ReturnPromise ready(final Consumer<Response> onSuccess) {
        final Consumer<Response> oldOnSuccess = this.onSuccess;
        this.onSuccess = a -> {
            oldOnSuccess.accept(a);
            onSuccess.accept(a);
        };
        return this;
    }

    @Override
    public ReturnPromise error(final BiConsumer<Integer, String> onError) {
        final BiConsumer<Integer, String> oldOnError = this.onError;
        this.onError = (a, b) -> {
            oldOnError.accept(a, b);
            onError.accept(a, b);
        };
        if (null != caughtError) {
            onError.accept(0, caughtError.getMessage());
        }
        return this;
    }

    @Override
    public ReturnPromise finalize(final Consumer<Optional<Response>> afterCompletion) {
        final Consumer<Optional<Response>> oldAfterCompletion = this.afterCompletion;
        this.afterCompletion = a -> {
            oldAfterCompletion.accept(a);
            afterCompletion.accept(a);
        };
        if (null != caughtError) {
            afterCompletion.accept(Optional.empty());
        }
        return this;
    }

    protected void handleResponse(final Exception exception) {
        LOGGER.t("HTTP error: %s", exception.getMessage());
        onError.accept(-1, exception.getMessage());
        afterCompletion.accept(Optional.empty());
    }

    protected void handleResponse(final Response response) {
        final int statusCode = response.getStatusCode();
        LOGGER.t("HTTP response status: %d", statusCode);
        if (statusCode > 199 && statusCode < 400) {
            onSuccess.accept(response);
        } else {
            onError.accept(statusCode, response.getFullBody().orElse(""));
        }
        afterCompletion.accept(Optional.of(response));
    }
}