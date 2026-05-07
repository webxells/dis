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
package com.webxells.dis.server.rest.filter.auth;

import com.webxells.dis.api.rest.Request;
import com.webxells.dis.api.rest.filter.RequestFilter;
import java.util.Optional;

public class BearerToken implements RequestFilter {
    private String token;

    @Override
    public Optional<ReturnState> resolve(final Request request) {
        return Optional.ofNullable(request.getHeaders())
                .map(a -> a.get("authorization"))
                .filter(a -> !a.isEmpty())
                .filter(a -> !a.get(0).equals("Bearer ".concat(token)))
                .map(a -> ReturnState.UNAUTHORIZED);
    }

    public void setToken(final String token) {
        this.token = token;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }
}