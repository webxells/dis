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
package com.webxells.dis.rest.error;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Description("Retries http call before raising an error")
public class Retry extends Error {
    class AsyncContainer {
        private final Client client;
        private final Request request;
        private final AtomicInteger current = new AtomicInteger(amount);

        public AsyncContainer(final Client client, final Request request) {
            this.client = client;
            this.request = request;
        }

        private void go() {
            client.sendAsync(request)
                    .error((i, m) -> {
                        LOGGER.e("Rest Error (%d): %s", i, m);
                        if (current.decrementAndGet() > 0) {
                            if (timeoutInMilliseconds > 0) {
                                threadWait();
                            }
                            go();
                        } else {
                            LOGGER.w("Max amount of retries reached");
                        }
                    })
                    .finalize(Retry.this::clearBody);
        }
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(Retry.class);

    @Description("Number of retry attempts")
    @Default("9999")
    private int amount;

    @Description("Time to wait between each retry")
    @Default("No timeout")
    private int timeoutInMilliseconds;

    @Override
    public Response executeRest(final Client client, final Request request) throws IOException {
        for (int i = amount < 1 ? 9999 : amount; i > 0; i--) {
            try {
                return super.executeRest(client, request);
            } catch (final IOException | InterruptedException e) {
                LOGGER.e(e);
                if (timeoutInMilliseconds > 0 && amount > 1) {
                    threadWait();
                }
            }
        }
        throw new IOException("Max amount of retries reached");
    }
    @Override
    public void executeAsyncRest(final Client client, final Request request) {
        new AsyncContainer(client, request).go();
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public void setTimeoutInMilliseconds(final int timeoutInMilliseconds) {
        this.timeoutInMilliseconds = timeoutInMilliseconds;
    }

    private void threadWait() {
        LOGGER.debug(String.format("Waiting for next retry: %s ms ", timeoutInMilliseconds));
        try {
            Thread.sleep(timeoutInMilliseconds);
        } catch (final InterruptedException ignored) {
            //shut down
        }
    }
}