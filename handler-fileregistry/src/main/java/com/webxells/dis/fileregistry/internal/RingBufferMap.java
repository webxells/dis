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
package com.webxells.dis.fileregistry.internal;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public class RingBufferMap<T, R> {
    private final LinkedHashMap<T, R> map;
    private final int capacity;

    public RingBufferMap(final int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity is required to be greater 0");
        }
        this.capacity = capacity;
        map = new LinkedHashMap<>(capacity, 2f);
    }

    public void put(final T key, final R value) {
        assertNInCapacity(1);
        map.put(key, value);
    }

    public void putAll(final Map<T, R> otherMap) {
        assertNInCapacity(Objects.requireNonNull(otherMap).size());
        this.map.putAll(otherMap);
    }

    public int size() {
        return map.size();
    }

    public R get(final T key) {
        return map.get(key);
    }

    public boolean containsKey(final T key) {
        return map.containsKey(key);
    }

    public void clear() {
        map.clear();
    }

    private synchronized void assertNInCapacity(final int n) {
        final Iterator<Map.Entry<T, R>> iterator = map.entrySet().iterator();
        for (int i = (map.size() + n) - capacity; i > 0; i--) {
            iterator.next();
            iterator.remove();
        }
    }
}