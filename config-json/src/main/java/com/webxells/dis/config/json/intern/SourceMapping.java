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
package com.webxells.dis.config.json.intern;

import com.google.gson.JsonElement;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.webxells.dis.boot.ConfigurationMapping;
import java.io.IOException;
import java.util.Optional;

public class SourceMapping {
    private static final String MAPPING_KEY = createMappingKey();

    private static String createMappingKey() {
        return "dison-source-mapping=".concat(
                Optional.ofNullable(System.getProperty("dison-source-mapping-key"))
                        .map(a -> a.replaceAll("\\s+|\"", ""))
                        .orElse(String.valueOf(System.currentTimeMillis()))
        );
    }

    public static String getMappingKey() {
        return MAPPING_KEY;
    }

    public static void register(final JsonReader in, final Object instance) throws IOException {
        if (in.peek() != JsonToken.STRING) {
            throw new IllegalArgumentException("invalid dison source mapping: string expected, got " + in.peek());
        }
        ConfigurationMapping.setLocation(instance, in.nextString());
    }

    public static void register(final JsonElement element, final Object instance) {
        if (!element.isJsonPrimitive()) {
            throw new IllegalArgumentException("invalid dison source mapping: string expected, got " + element.getClass());
        }
        ConfigurationMapping.setLocation(instance, element.getAsString());
    }
}