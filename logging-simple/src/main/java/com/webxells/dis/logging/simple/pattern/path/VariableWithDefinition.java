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
package com.webxells.dis.logging.simple.pattern.path;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class VariableWithDefinition implements PatternEntity {
    protected final Map<String, String> definition;
    public VariableWithDefinition(final String definition) {
        this.definition = empty(definition) ? Map.of() : extract(new StringBuilder(definition));
    }

    private boolean empty(final String definition) {
        return null == definition || definition.isEmpty();
    }

    private Map<String, String> extract(final StringBuilder definition) {
        final Map<String, String> result = new HashMap<>();
        while(true) {
            final int current = definition.indexOf(":");
            if (current < 0) break;
            final String name = definition.substring(0, current).trim();
            int till = definition.indexOf(",", current + 1);
            if (till < 0) {
                till = definition.length();
            }
            result.put(name, definition.substring(current + 1, till));
            definition.delete(0, till + 1);
        }
        return result;
    }

    protected int defineInt(final String name, final int defaultInt) {
        return get(name)
                .map(Integer::parseInt)
                .orElse(defaultInt);
    }

    protected boolean defineBoolean(final String name, final boolean defaultBool) {
        return get(name)
                .map(a -> "true".equalsIgnoreCase(a) || "1".equals(a))
                .orElse(defaultBool);
    }

    protected String defineString(final String name, final String defaultString) {
        return get(name).orElse(defaultString);
    }

    @SuppressWarnings("unchecked")
    protected <T extends Enum<? extends Enum<?>>> T defineEnum(final String name, final T defaultValue) {
        if (null == defaultValue) {
            throw new IllegalArgumentException("Default value cannot be null - required to iterate through enum");
        }
        return get(name)
                .flatMap(a -> Arrays.stream(((Class<T>) defaultValue.getClass())
                                .getEnumConstants())
                        .filter(b -> b.name().equals(a))
                        .findAny())
                .orElse(defaultValue);
    }

    private Optional<String> get(final String name) {
        return Optional.ofNullable(definition.get(name));
    }
}