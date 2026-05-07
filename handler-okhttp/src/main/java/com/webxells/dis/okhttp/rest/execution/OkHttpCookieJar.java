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

import com.webxells.dis.rest.cookie.CookieJar;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import okhttp3.Cookie;
import okhttp3.HttpUrl;

public class OkHttpCookieJar implements okhttp3.CookieJar {
    private final CookieJar cookieJar;

    public OkHttpCookieJar(final CookieJar cookieJar) {
        this.cookieJar = cookieJar;
    }

    @Override
    public List<Cookie> loadForRequest(final HttpUrl httpUrl) {
        try {
            return cookieJar.get(httpUrl.uri(), Map.of()).values().stream()
                    .flatMap(a -> parseToCookies(httpUrl, a).stream())
                    .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new RuntimeException("Could not parse cookies", e);
        }

    }

    @Override
    public void saveFromResponse(final HttpUrl httpUrl, final List<Cookie> list) {
        try {
            cookieJar.put(httpUrl.uri(), Map.of("Set-Cookie", parseFromCookies(list)));
        } catch (final IOException e) {
            throw new RuntimeException("Could not process cookies", e);
        }
    }

    private List<Cookie> parseToCookies(final HttpUrl httpUrl, final List<String> cookies) {
        return cookies.stream()
                .map(a -> Cookie.parse(httpUrl, a))
                .collect(Collectors.toList());
    }

    private List<String> parseFromCookies(final List<Cookie> list) {
        return list.stream().map(Cookie::toString).collect(Collectors.toList());
    }
}