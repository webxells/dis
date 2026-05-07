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
package org.slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class BasicMarkerFactory implements IMarkerFactory {
    private static final Map<String, Marker> REGISTERED_MARKERS = new ConcurrentHashMap<>();

    @Override
    public boolean detachMarker(final String name) {
        return REGISTERED_MARKERS.remove(name) != null;
    }

    @Override
    public boolean exists(final String name) {
        return REGISTERED_MARKERS.containsKey(name);
    }

    @Override
    public Marker getDetachedMarker(final String name) {
        return REGISTERED_MARKERS.remove(name);
    }

    @Override
    public Marker getMarker(final String name) {
        return REGISTERED_MARKERS.computeIfAbsent(name, a -> new NullMarker());
    }
}