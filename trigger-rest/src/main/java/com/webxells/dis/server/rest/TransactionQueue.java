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
package com.webxells.dis.server.rest;

import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TransactionQueue<T> {
    private static final Map<String, TransactionQueue<?>> REGISTRY = new ConcurrentHashMap<>();

    private final Queue<T> queue = new ConcurrentLinkedQueue<>();
    private T current;

    public static <T> TransactionQueue<T> createNewQueue(final String index) {
        final TransactionQueue<T> newInstance = new TransactionQueue<>();
        REGISTRY.put(index, newInstance);
        return newInstance;
    }

    @SuppressWarnings("unchecked")
    public static <T> TransactionQueue<T> getQueue(final String index) {
        return Optional.ofNullable(REGISTRY.get(index))
                .map(a  -> (TransactionQueue<T>) a)
                .orElseGet(() -> createNewQueue(index));
    }

    public void add(final T request) {
        queue.add(request);
    }

    public void reset() {
        queue.clear();
    }

    public T next() {
         current = queue.poll();
         return current;
    }

    public T current() {
        return current;
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public int size() {
        return queue.size();
    }
}