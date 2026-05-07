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

import com.webxells.dis.memory.indexer.IndexAltering;
import com.webxells.dis.memory.registry.MemoryRegistry.Transaction;
import com.webxells.dis.hash.engine.Murmur3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IndexedRegistry implements RegistryStrategy {
    private class IndexIterator implements Iterator<Transaction> {
        private final Iterator<String> mapLevel = indexed.keySet().iterator();
        private Iterator<Transaction> listLevel;

        @Override
        public boolean hasNext() {
            refreshLevel();
            return listLevel.hasNext();
        }

        @Override
        public Transaction next() {
            refreshLevel();
            return listLevel.next();
        }

        private void refreshLevel() {
            if ((null == listLevel || !listLevel.hasNext()) && mapLevel.hasNext()) {
                final String next = mapLevel.next();
                listLevel = indexed.get(next).iterator();
            }
        }
    }
    private static final String DELIMITER = String.valueOf(System.identityHashCode(IndexedRegistry.class));
    private static final Murmur3 ENGINE = new Murmur3();
    private static final List<String> NON_FOUND_VALUE = List.of(DELIMITER, "NOT_FOUND");

    private final Map<String, List<Transaction>> indexed = new HashMap<>();
    private final Set<String> indexFields;
    private final List<IndexAltering> indexAltering;

    public IndexedRegistry(final Set<String> indexFields) {
        this(indexFields, null);
    }

    public IndexedRegistry(final Set<String> indexFields, final List<IndexAltering> indexAltering) {
        this.indexFields = indexFields;
        this.indexAltering = Objects.requireNonNullElseGet(indexAltering, List::of);
    }

    @Override
    public Transaction newTransaction(final Map<String, List<String>> values) {
        final Transaction transaction = new Transaction();
        values.forEach(transaction::add);
        indexed.computeIfAbsent(createIndex(values), a -> new ArrayList<>())
                .add(transaction);
        return transaction;
    }

    private String createIndex(final Map<String, List<String>> values) {
        final StringBuilder result = new StringBuilder();
        indexFields.forEach(a -> result.append(String.format("%s_%s-%s", a, DELIMITER, getIndexValues(a, values))));
        return ENGINE.convert(result.toString());
    }

    private String getIndexValues(final String index, final Map<String, List<String>> values) {
        return values.getOrDefault(index, NON_FOUND_VALUE).stream()
                .map(a -> {
                    String result = a;
                    for (IndexAltering altering : indexAltering) {
                        result = altering.index(result);
                    }
                    return result;
                })
                .collect(Collectors.joining(DELIMITER));
    }

    @Override
    public Iterator<Transaction> iterator() {
        return new IndexIterator();
    }

    @Override
    public Stream<Map<String, List<String>>> lookUp(final Map<String, List<String>> conditions) {
        return Optional.ofNullable(indexed.get(createIndex(conditions))).stream()
                .flatMap(Collection::stream)
                .map(Transaction::values);
    }

}