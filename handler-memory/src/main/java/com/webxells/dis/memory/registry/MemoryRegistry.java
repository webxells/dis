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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MemoryRegistry {
    public static class Transaction {

        Transaction() {}

        private final Map<String, List<String>> values = new HashMap<>();

        public void add(final String path, final String value) {
            values.computeIfAbsent(path, a -> new LinkedList<>())
                    .add(value);
        }
        public void add(final String path, final List<String> value) {
            values.put(path, value);
        }
        public List<String> get(final String path) {
            return values.getOrDefault(path, null);
        }

        public boolean has(final String path) { return values.containsKey(path); }

        public Map<String, List<String>> values() { return values; }

    }

    protected static final Map<String, MemoryRegistry> instanceRegistry = new HashMap<>();
    protected final RegistryStrategy registry;

    private Iterator<Transaction> reader;

    public static MemoryRegistry getFreshInstance(final String name, final RegistryStrategy strategy) {
        instanceRegistry.remove(name);
        return getInstance(name, strategy);
    }

    public static MemoryRegistry getInstance(final String index, final RegistryStrategy strategy) {
        return instanceRegistry.computeIfAbsent(index, a -> new MemoryRegistry(strategy));
    }

    public static MemoryRegistry getExistent(final String index) {
        return instanceRegistry.get(index);
    }

    protected MemoryRegistry(final RegistryStrategy strategy) {
        this.registry = strategy;
    }

    public List<Map<String, List<String>>> lookForMany(final Map<String, List<String>> conditions) {
        if (!conditions.isEmpty()) {
            return filterForTransactions(conditions)
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    public Optional<Map<String, List<String>>> lookForOne(final Map<String, List<String>> conditions) {
        if (!conditions.isEmpty()) {
            return filterForTransactions(conditions)
                    .findAny();
        }
        return Optional.empty();
    }

    public void rollback() {
        reader = null;
    }

    private Stream<Map<String, List<String>>> filterForTransactions(final Map<String, List<String>> conditions) {
        return registry.lookUp(conditions);
    }

    public Transaction createTransaction(final Map<String, List<String>> values) {
        return registry.newTransaction(values);
    }

    public boolean hasNext() {
        return getReader().hasNext();
    }

    public Transaction readNext() {
        return getReader().next();
    }

    private Iterator<Transaction> getReader() {
        if (null == reader) {
            reader = registry.iterator();
        }
        return reader;
    }
}