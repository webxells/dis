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
package com.webxells.dis.boot;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class KnownTypeMapping {
    private static final Map<Class<?>, String> KNOWN = new HashMap<>();
    private static final Map<Class<?>, String> FORCED = new HashMap<>();

    public static boolean isKnown(final Class<?> rawType) {
        return KNOWN.containsKey(rawType);
    }

    public static String getForced(final Class<?> rawType) {
        return FORCED.get(rawType);
    }

    public static String getKnown(final Class<?> rawType) {
        return KNOWN.get(rawType);
    }

    public static <T> Class<? extends T> mapClass(final Class<T> clazz) {
        return map(KNOWN, clazz);
    }
    
    public static <T> Class<? extends T> getForcedMapping(final Class<T> clazz) {
        return map(FORCED, clazz);
    }

    private static <T> Class<? extends T> map(final Map<Class<?>, String> map, final Class<T> clazz) {
        if (map.containsKey(clazz)) {
            return (Class<? extends T>) toClass(map.get(clazz));
        }
        return clazz;
    }

    public static Map<Class<?>, String> getKnown() {
        return Collections.unmodifiableMap(KNOWN);
    }

    private static Class<?> toClass(final String s) {
        try {
            return Class.forName(s);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static void add(final Class<?> type, final String mapping) {
        KNOWN.put(type, mapping);
    }
    
    public static void force(final Class<?> type, final String mapping) {
        FORCED.put(type, mapping);
    }

}