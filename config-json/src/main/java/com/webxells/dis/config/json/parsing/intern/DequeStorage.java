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
package com.webxells.dis.config.json.parsing.intern;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class DequeStorage {
    private final Deque<Map<String, DisonElementReader>> tempRegistry = new LinkedList<>();


    public Optional<DisonElementReader> get(final String name) {
        final Iterator<Map<String, DisonElementReader>> iterator = iterator();
        while (iterator.hasNext()) {
            final Map<String, DisonElementReader> current = iterator.next();
            if (current.containsKey(name)) {
                return Optional.of(current.get(name));
            }
        }
        return Optional.empty();
    }

    public void add(final String name, final DisonElementReader value) {
        if (tempRegistry.isEmpty()) {
            tempRegistry.add(new HashMap<>());
        }
        tempRegistry.getLast().put(name, value);
    }

    public void addLast(final Map<String, DisonElementReader> map) {
        tempRegistry.addLast(map);
    }

    public void removeLast() {
        tempRegistry.removeLast();
    }

    public Iterator<Map<String, DisonElementReader>> iterator() {
        return tempRegistry.descendingIterator();
    }

    public void forEach(final Consumer<Map<String, DisonElementReader>> consumer) {
        tempRegistry.forEach(consumer);
    }
}