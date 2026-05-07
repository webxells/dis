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

import com.webxells.dis.api.output.Output;
import com.webxells.dis.memory.indexer.IndexAltering;
import com.webxells.dis.memory.registry.IndexedRegistry;
import com.webxells.dis.memory.registry.MemoryRegistry;
import com.webxells.dis.memory.registry.RegistryStrategy;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IndexedMemoryOutput extends SimpleMemoryOutput implements Output<IndexedMemoryOutputConfig> {
    private final Set<String> indexFields;
    private final List<IndexAltering> indexAltering;

    public IndexedMemoryOutput(final IndexedMemoryOutputConfig config) {
        super(config);
        indexFields = config.getIndexFields();
        indexAltering = config.getIndexAltering();
    }

    @Override
    protected void saveValues(final Map<String, List<String>> values) {
        getOutputRegistry(indexFields).createTransaction(values);
    }

    private MemoryRegistry getOutputRegistry(final Set<String> index) {
        if (null == outputRegistry) {
            RegistryStrategy strategy = new IndexedRegistry(index, indexAltering);
            outputRegistry = append ? MemoryRegistry.getInstance(name, strategy) : MemoryRegistry.getFreshInstance(name, strategy);
        }
        return outputRegistry;
    }

}