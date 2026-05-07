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
package com.webxells.dis.test.example.discover;

import com.webxells.dis.api.discover.DiscoverResult;
import com.webxells.dis.api.discover.PathDiscoverer;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDiscoverer implements PathDiscoverer.Run {
    public static class Entry {
        private final String key;
        private final String value;

        public Entry(final String key, final String value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public String getValue() {
            return value;
        }
    }
    private final List<Entry> content;
    private int current;

    public TestDiscoverer(final List<Entry> content) {
        this.content = content;
    }
    @Override
    public void investigate(final String path, final String value) {
        assertEquals(content.get(current).getKey(), path);
        assertEquals(content.get(current).getValue(), value);
        current++;
    }

    @Override
    public Map<String, List<DiscoverResult>> summarize() {
        return null;
    }

    @Override
    public String name() {
        return null;
    }

    public int getCurrent() {
        return current;
    }
}