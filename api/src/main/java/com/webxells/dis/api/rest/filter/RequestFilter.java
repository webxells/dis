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
package com.webxells.dis.api.rest.filter;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.rest.Request;
import java.util.Optional;

public interface RequestFilter extends ConfigurableByType {
    enum ReturnState {
        BAD_REQUEST(400), UNAUTHORIZED(401), FORBIDDEN(403), NOT_FOUND(404),
        NOT_ACCEPTABLE(406);

        private final int httpState;

        ReturnState(final int httpState) {
            this.httpState = httpState;
        }

        public int getHttpState() {
            return httpState;
        }
    }
    Optional<ReturnState> resolve(Request request);
}