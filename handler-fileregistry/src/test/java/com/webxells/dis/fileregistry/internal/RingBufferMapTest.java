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

import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RingBufferMapTest {

    @Test
    void illegalArguments() {
        assertThrows(IllegalArgumentException.class, () -> new RingBufferMap<>(0));
        assertThrows(IllegalArgumentException.class, () -> new RingBufferMap<>(-1));
        assertThrows(NullPointerException.class, () -> new RingBufferMap<>(2).putAll(null));
        assertDoesNotThrow(() -> new RingBufferMap<>(2).put(null, new Object()));
    }

    @Test
    void test() {
        int capacity = Integer.parseInt("" + Math.round(Math.random() * 50 + 1));
        RingBufferMap<Integer, Integer> fixture = new RingBufferMap<>(capacity);
        assertEquals(0, fixture.size());
        for(int i = 0; i < capacity; i++) {
            fixture.put(i, i);
        }
        assertEquals(0, fixture.get(0));
        assertEquals(capacity, fixture.size());
        assertEquals(capacity - 1, fixture.get(capacity - 1));
        assertTrue(fixture.containsKey(0));
        assertEquals(0, fixture.get(0));
        fixture.put(capacity, capacity);
        assertEquals(capacity, fixture.size());
        assertFalse(fixture.containsKey(0));
        fixture.putAll(IntStream.range(capacity + 1, capacity * 2).boxed().collect(Collectors.toMap(x -> x, y -> y)));
        assertEquals(capacity, fixture.size());
        assertFalse(fixture.containsKey(0));
        assertTrue(fixture.containsKey(capacity));
        fixture.clear();
        assertEquals(0, fixture.size());
    }

}