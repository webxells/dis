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
package com.webxells.dis.rest.cookie;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DisJar implements CookieJar {
    private Map<String, String> defaultCookies;
    private final Map<String, String> receivedCookies = new HashMap<>();

    public DisJar() { }

    public DisJar(final Map<String, String> defaultCookies) {
        setDefaultCookies(defaultCookies);
    }

    public void setDefaultCookies(final Map<String, String> defaultCookies) {
        this.defaultCookies = defaultCookies;
    }

    public Map<String, String> getReceivedCookies() {
        return receivedCookies;
    }

    @Override
    public Map<String, List<String>> get(final URI uri, final Map<String, List<String>> requestHeaders) throws IOException {
        List<String> cookies = new LinkedList<>();
        Optional.ofNullable(defaultCookies)
                .ifPresent(a -> addCookies(a, cookies));
        addCookies(receivedCookies, cookies);
        return Collections.singletonMap("Cookie", cookies);
    }

    private void addCookies(final Map<String, String> cookieMap, final List<String> cookieList) {
        cookieMap.forEach((key, value) -> cookieList.add(String.format("%s=%s", key, value)));
    }

    @Override
    public void put(final URI uri, final Map<String, List<String>> responseHeaders) throws IOException {
        List<String> cookieList = responseHeaders.get("Set-Cookie");
        if (null != cookieList) {
            cookieList.forEach(this::saveCookie);
        }
    }

    private void saveCookie(final String item) {
        String[] parts = item.split(";");
        if (parts.length > 0) {
            String[] nameValue = parts[0].trim().split("=");
            if (nameValue.length > 0) {
                receivedCookies.put(nameValue[0].trim(), String.join("=", Arrays.copyOfRange(nameValue, 1,
                        nameValue.length)).trim());
            }
        }
    }
}