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

import com.webxells.dis.api.rest.Request;

import java.io.IOException;

public class Accept implements HttpTransaction {
    public record SimplyForwarding(Request request) implements Entity {

    }

    @Override
    public Entity newEntity(final Request request) {
        try {
            request.respond().send(202);
        } catch (final IOException e) {
            throw new RuntimeException("Could not send request", e);
        }
        return new SimplyForwarding(request);
    }
}