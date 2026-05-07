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
import com.webxells.dis.rest.execution.Client;
import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.IOException;
import java.util.Optional;

public class Error implements ErrorStrategy {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Error.class);

    @Override
    public Response executeRest(final Client client, final Request request) throws IOException, InterruptedException {
        final Response response = client.send(request);
        assertValidResponse(response);
        return response;
    }

    @Override
    public void executeAsyncRest(final Client client, final Request request) throws IOException {
        client.sendAsync(request)
                .error(this::logThis)
                .finalize(this::clearBody);
    }

    protected void logThis(Integer status, String error) {
        LOGGER.error("Error executing rest request (%d): %s", status, error);
    }

    protected void assertValidResponse(final Response response) throws IOException {
        if (isInvalidResponseCode(response)) {
            logBody(response);
            throw new IOException(String.format("Got an invalid response: %s", response.getStatusCode()));
        }
    }

    protected void logBody(final Response response) {
        response.getFullBody()
                .ifPresent(a -> LOGGER.debug("Error response message: ".concat(a)));
    }


    protected boolean isInvalidResponseCode(final Response response) {
        return 200 > response.getStatusCode() || 209 < response.getStatusCode();
    }

    protected void clearBody(final Optional<Response> response) {
        response
                .map(Response::body)
                .ifPresent(a -> {
                    try {
                        a.close();
                    } catch (final IOException ignore) {
                        //skip - we dont need this
                    }
                });
    }
}