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
package com.webxells.dis.json.discover;

import java.util.regex.Pattern;

public class JsonValue {
    public static final Pattern ARRAY_KEYS = Pattern.compile("\\[(\\d+)]");
    private final String value;
    private final String path;

    public JsonValue(final String value, final String path) {
        this.value = value;
        this.path = path;
    }

    public String value() {
        return value;
    }

    public String path() {
        return path;
    }

    public String generalizedPath() {
        return ARRAY_KEYS.matcher(path).replaceAll("[]");
    }
}