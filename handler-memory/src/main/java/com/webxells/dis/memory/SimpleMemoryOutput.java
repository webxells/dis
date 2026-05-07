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
import com.webxells.dis.memory.registry.ListRegistry;
import com.webxells.dis.memory.registry.MemoryRegistry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

abstract class SimpleMemoryOutput {
    final String name;
    public final boolean overwrite;
    public final boolean append;
    MemoryRegistry outputRegistry;

    public SimpleMemoryOutput(final MemoryOutputConfig config) {
        name = config.getName();
        overwrite = config.isOverwrite();
        append = config.isAppend();
    }

    public void write(final MappingConfiguration to) {
        final Map<String, List<String>> values = new HashMap<>();
        to.partsByDestination(name).forEach(part -> part.getDataset().getContent().forEach(
                a -> values.computeIfAbsent(part.getOutput().getPath(), b -> new ArrayList<>()).add(a.value()
                        .orElse(""))));
        saveValues(values);
    }

    protected void saveValues(final Map<String, List<String>> values) {
        final MemoryRegistry.Transaction transaction = getOutputRegistry().createTransaction(values);
        values.forEach(transaction::add);
    }

    public String getName() {
        return name;
    }

    public void start() {
        if (overwrite) {
            outputRegistry = null;
        }
    }

    public void end() { }

    private MemoryRegistry getOutputRegistry() {
        if (null == outputRegistry) {
            outputRegistry = append ? MemoryRegistry.getInstance(name, new ListRegistry()) : MemoryRegistry.getFreshInstance(name, new ListRegistry());
        }
        return outputRegistry;
    }
}