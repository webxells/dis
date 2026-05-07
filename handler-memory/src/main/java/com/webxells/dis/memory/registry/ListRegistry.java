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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ListRegistry implements RegistryStrategy {
    private final List<Transaction> list = new ArrayList<>();
    private final List<IndexAltering> indexAltering;

    public ListRegistry() {
        this(null);
    }

    public ListRegistry(final List<IndexAltering> indexAltering) {
        this.indexAltering = indexAltering;
    }

    @Override
    public Transaction newTransaction(final Map<String, List<String>> values) {
        Transaction transaction = new Transaction();
        list.add(transaction);
        values.forEach(transaction::add);
        return transaction;
    }

    @Override
    public Iterator<Transaction> iterator() {
        return list.iterator();
    }

    @Override
    public Stream<Map<String, List<String>>> lookUp(final Map<String, List<String>> conditions) {
        return list.stream()
                .filter(a -> conditions.entrySet().stream().allMatch(
                        b -> a.has(b.getKey()) && valueMatch(a, b)))
                .map(Transaction::values);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Transaction pop() {
        return list.removeFirst();
    }

    private boolean valueMatch(final Transaction transaction, final Map.Entry<String, List<String>> condition) {
        final List<String> conditionValues = indexValues(condition.getValue());
        final List<String> transactionValues = indexValues(transaction.get(condition.getKey()));
        return conditionValues.containsAll(transactionValues) || transactionValues.containsAll(conditionValues);
    }

    private List<String> indexValues(final List<String> value) {
        if (null == indexAltering) {
            return value;
        }
        return value.stream()
                .map(AtomicReference::new)
                .map(a -> {
                    indexAltering.forEach(b -> a.set(b.index(a.get())));
                    return a.get();
                })
                .collect(Collectors.toList());
    }
}