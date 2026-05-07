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
package com.webxells.dis.memory.registry;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public final class VariableRegistry {
    private static final String DELIMITER = Objects.toIdentityString(VariableRegistry.class);

    private static final Map<String, List<String>> REGISTRY = new HashMap<>();

    public static void set(final String name, final List<String> values) {
        REGISTRY.put(createName(name), values);
    }

    public static List<String> get(final String name) {
        return REGISTRY.get(createName(name));
    }

    private static String createName(final String name) {
        return String.format("%s-%s-%s", Thread.currentThread().getName(), DELIMITER, name);
    }

    public static Stream<String> getAsStream(final String name) {
        return Optional.ofNullable(get(name)).stream()
                .flatMap(Collection::stream);
    }
}