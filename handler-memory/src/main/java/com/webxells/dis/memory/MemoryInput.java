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
package com.webxells.dis.memory;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.memory.registry.MemoryRegistry;
import com.webxells.dis.memory.registry.PoppingMemoryRegistry;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class MemoryInput implements Input<MemoryInputConfig> {
    private final String name;
    public final boolean persistent;
    private final boolean popping;
    private MemoryRegistry inputRegistry;

    public MemoryInput(final MemoryInputConfig config) {
        name = config.getName();
        persistent = config.isPersistent();
        popping = config.isPopInsteadOfRead();
    }

    @Override
    public int read(final MappingConfiguration from) {
        final AtomicInteger counter = new AtomicInteger();
        if (hasNext()) {
            final MemoryRegistry.Transaction transaction = inputRegistry.readNext();
            from.partsBySource(name).forEach(a -> {
                final List<String> values = transaction.get(a.getInput().getPath());
                if (null != values) {
                    values.forEach(b -> {
                        a.getDataset().collect(new SimpleDatasetPiece(b));
                        counter.getAndIncrement();
                    });
                }
            });
        }
        return counter.get();
    }

    @Override
    public boolean hasNext() {
        return getInputRegistry()
                .map(MemoryRegistry::hasNext)
                .orElse(false);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        if (persistent || popping) {
            getInputRegistry()
                    .ifPresent(MemoryRegistry::rollback);
            inputRegistry = null;
        }
    }

    @Override
    public void end() { }

    private Optional<MemoryRegistry> getInputRegistry() {
        if (null == inputRegistry) {
            inputRegistry = popping ? PoppingMemoryRegistry.getInstance(name) : MemoryRegistry.getExistent(name);
        }
        return Optional.ofNullable(inputRegistry);
    }
}