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
package com.webxells.dis.rest.cache;

import com.webxells.dis.rest.execution.Request;
import com.webxells.dis.rest.execution.Response;

public class NoCache implements CacheStrategy {
    private static class NoCacheEntity implements CacheEntity {
        private final Request request;
        private Response response;

        public NoCacheEntity(final Request request) {
            this.request = request;
        }

        @Override
        public boolean requiresRevalidation() {
            return true;
        }

        @Override
        public void set(final Response response) {
            this.response = response;
        }

        @Override
        public Response response() {
            return response;
        }

        @Override
        public Request request() {
            return request;
        }
    }

    private static final NoCache instance = new NoCache();

    public static NoCache instance() {
        return instance;
    }

    @Override
    public CacheEntity request(final Request request) {
        return new NoCacheEntity(request);
    }
}