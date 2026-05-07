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

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.rest.cookie.CookieJar;
import java.time.temporal.ChronoUnit;

public interface RestStrategy extends ConfigurableByType {
    public interface Builder {
        public Builder followRedirects(final HttpRedirect httpRedirect);
        public Builder cookieJar(final CookieJar cookiePersistenceStrategy);
        public Builder connectTimeout(final int timeout, final ChronoUnit unit);
        public Builder version(final HttpVersion httpVersion);
        public Builder disableSslVerification();
        public Builder priority(final int priority);
        public Client build();
    }

    public Builder newClient();
}