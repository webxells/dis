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
package com.webxells.dis.server.rest.transaction;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.Response;
import java.io.IOException;

public class WaitForResponse implements HttpTransaction {

    public class ProvidingResponse implements Entity {
        private final Request request;
        public ProvidingResponse(final Request request) {
            this.request = new WaitingRequest(request, gatewayTimeout);
        }

        @Override
        public Request request() {
            return request;
        }
    }

    @Description("In seconds, only greater 0 is recognized")
    private int gatewayTimeout;
    private Request last;

    public void setGatewayTimeout(final int gatewayTimeout) {
        this.gatewayTimeout = gatewayTimeout;
    }

    @Override
    public Entity newEntity(final Request request) {
        cancelLastIfNotYetFinished();
        last = request;
        return new ProvidingResponse(request);
    }

    private void cancelLastIfNotYetFinished() {
        if (null != last && Response.ResponseState.SENT != last.respond().state()) {
            try {
                last.respond().setStatus(500);
                last.respond().send();
            } catch (final IOException e) {
                throw new RuntimeException("could not send canceling response", e);
            }
        }
    }
}