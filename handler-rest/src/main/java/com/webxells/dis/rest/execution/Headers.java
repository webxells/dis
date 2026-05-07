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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

public class Headers {
    private Map<String, List<String>> content = new HashMap<>();

    public void add(final String key, final String value) {
        content.computeIfAbsent(key, k -> new LinkedList<>()).add(value);
    }

    public void set(final String key, final String value) {
        set(key, new LinkedList<>(List.of(value)));
    }

    public void set(final String key, final List<String> value) {
        content.put(key, value);
    }

    public Optional<String> firstValue(final String key) {
        return Optional.ofNullable(content.get(key))
                .filter(a -> !a.isEmpty())
                .map(a -> a.get(0));
    }

    public Stream<Map.Entry<String, List<String>>> stream() {
        return content.entrySet().stream();
    }

    public Set<String> keys() {
        return content.keySet();
    }


    public List<String> get(final String key) {
        return Optional.ofNullable(content.get(key)).orElse(List.of());
    }
}