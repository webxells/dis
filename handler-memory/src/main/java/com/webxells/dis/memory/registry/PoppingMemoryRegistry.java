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

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PoppingMemoryRegistry extends MemoryRegistry {
    private final MemoryRegistry child;

    public static MemoryRegistry getInstance(final String index) {
        return Optional.ofNullable(MemoryRegistry.getExistent(index))
                .map(PoppingMemoryRegistry::new)
                .orElse(null);
    }

    public PoppingMemoryRegistry(final MemoryRegistry memoryRegistry) {
        super(null);
        child = memoryRegistry;
    }

    @Override
    public boolean hasNext() {
        return !((ListRegistry) child.registry).isEmpty();
    }

    @Override
    public Transaction readNext() {
        return ((ListRegistry) child.registry).pop();
    }

    @Override
    public List<Map<String, List<String>>> lookForMany(final Map<String, List<String>> conditions) {
        return child.lookForMany(conditions);
    }

    @Override
    public Optional<Map<String, List<String>>> lookForOne(final Map<String, List<String>> conditions) {
        return child.lookForOne(conditions);
    }

    @Override
    public void rollback() {
        child.rollback();
    }

    @Override
    public Transaction createTransaction(final Map<String, List<String>> values) {
        return child.createTransaction(values);
    }
}