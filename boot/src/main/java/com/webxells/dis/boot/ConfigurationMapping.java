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

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ConfigurationMapping {
    private static final Map<Object, String> MAPPING_REGISTRY = new ConcurrentHashMap<>();

    public static String getLocation(final Object object) {
        return MAPPING_REGISTRY.get(object);
    }

    public static String getFormatedLocation(final Object object) {
        return Optional.ofNullable(getLocation(object))
                .map(a -> String.format("%s from %s", object.getClass().getSimpleName(), a))
                .orElse(object.toString());
    }

    public static void setLocation(final Object object, final String location) {
        MAPPING_REGISTRY.put(object, location);
    }

    public static void clear() {
        MAPPING_REGISTRY.clear();
    }
}