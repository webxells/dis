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
package com.webxells.dis.memory.linker;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.DynamicMappingPart;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.base.input.linker.SimpleInputLinker;
import com.webxells.dis.memory.IndexedMemoryOutput;
import com.webxells.dis.memory.IndexedMemoryOutputConfig;
import com.webxells.dis.memory.indexer.IndexAltering;
import com.webxells.dis.memory.registry.MemoryRegistry;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Description("Reads entire Input into memory and only appends matching data to current MappingConfiguration")
public class MemoryMatchLink extends SimpleInputLinker {
    private static final Logger LOGGER = LoggerProxyFactory.logger(MemoryMatchLink.class);

    @Description("MappingParts that holding values that will be compared with")
    @Required
    private List<MappingPortrayal> links;
    @Description("IndexAltering may alter data for comparison")
    private List<IndexAltering> indexAltering;
    @Description("StableMappingParts are persistent and won't be removed")
    private boolean stablePartGeneration;
    @Description("Enable multiple values reading")
    private boolean createMultipleValues;
    @Description("If mapping parts for linking Input already exists, this should be enabled")
    private boolean skipMappingPartCreationForLinks;
    @Description("This will force linker to refresh data on every read")
    private boolean refreshOnRead;
    @Description("If Input is already read in by other Linker, you may not need to read data again and use other instead")
    private String usingExistingRegistry;
    private String name;
    private MemoryRegistry memoryRegistry;
    private IndexedMemoryOutput memoryOutput;

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        final Map<String, List<String>> condition = new HashMap<>();
        for (MappingPortrayal link : links) {
            final Optional<MappingPart> linkMapping = from.getByPortrayal(link);
            if (linkMapping.isPresent()) {
                condition.put(link.getPath(),
                        linkMapping.get().getDataset().getContent().stream()
                                .map(a -> a.value().orElse(null))
                                .collect(Collectors.toList()));
            } else {
                return 0;
            }
        }
        try {
            assertMemoryLoaded(from, condition.keySet());
        } catch (final DisException e) {
            throw new InputOutputError("Could not load memory", e);
        }
        return Math.toIntExact(lookForTransaction(condition).stream()
                .mapToInt(stringStringMap -> assignDataToMapping(stringStringMap,
                        skipMappingPartCreationForLinks ? Collections.emptySet() : condition.keySet(), from))
                .sum());
    }

    private List<Map<String, List<String>>> lookForTransaction(final Map<String, List<String>> condition) {
        return getRegistry()
                .map(a -> createMultipleValues ? a.lookForMany(condition) :
                        a.lookForOne(condition).stream().collect(Collectors.toList()))
                .orElse(List.of());
    }

    private void assertMemoryLoaded(final MappingConfiguration from, final Set<String> indexFields) throws DisException {
        name = Objects.requireNonNullElseGet(usingExistingRegistry, () -> getInput().getName());
        if (null == usingExistingRegistry && (null == memoryOutput || refreshOnRead)) {
            memoryRegistry = null;
            loadIntoMemory(from, indexFields);
        }
    }

    private int assignDataToMapping(final Map<String, List<String>> resultMap, final Set<String> ignore,
                                    final MappingConfiguration from) {
        final AtomicInteger counter = new AtomicInteger();
        resultMap.entrySet().stream()
                .filter(a -> !ignore.contains(a.getKey()))
                .forEach(a -> from.getByPortrayal(createPortrayal(a.getKey()))
                    .ifPresent(c -> {
                        c.getDataset().collect(a.getValue().stream()
                                .map(SimpleDatasetPiece::new)
                                .collect(Collectors.toList()));
                         counter.addAndGet(c.getDataset().getContent().size());
        }));
        return counter.get();
    }

    private MappingPortrayal createPortrayal(final String path) {
        return new SimpleMappingPortrayal() {{
            this.setReference(name);
            this.setPath(path);
        }};
    }

    private Optional<MemoryRegistry> getRegistry() {
        if (null == memoryRegistry) {
            memoryRegistry = MemoryRegistry.getExistent(Objects.requireNonNullElse(usingExistingRegistry, name));
        }
        return Optional.ofNullable(memoryRegistry);
    }

    private void loadIntoMemory(final MappingConfiguration from, final Set<String> indexFields) throws DisException {
        LOGGER.info("Loading data (%s) into memory", getInputName());
        final MappingConfiguration to = mapFromToTo(from);
        final Input<? extends InputConfig> input = getInput();
        memoryOutput = new IndexedMemoryOutput(new IndexedMemoryOutputConfig(name, indexFields, indexAltering));
        memoryOutput.start();
        input.start();
        while (input.hasNext()) {
            to.clear();
            input.read(to);
            memoryOutput.write(to);
        }
        memoryOutput.end();
        input.end();
        LOGGER.d("Finished loading data: %s", getInputName());
    }

    private MappingConfiguration mapFromToTo(final MappingConfiguration from) {
        final SimpleMappingConfiguration to = new SimpleMappingConfiguration();
        from.partsBySource(name).forEach(a -> to.addPart(createMappingPart(to, a)));
        if (!skipMappingPartCreationForLinks) {
            links.stream()
                    .map(mappingPortrayal -> createLinkMappingPart(mappingPortrayal, to))
                    .forEach(to::addPart);
        }
        return to;
    }

    private MappingPart createLinkMappingPart(final MappingPortrayal mappingPortrayal, final SimpleMappingConfiguration config) {
        final SimpleMappingPoint point = new SimpleMappingPoint(name, mappingPortrayal.getPath());
        return new StableMappingPart(config, point, point);
    }

    private MappingPart createMappingPart(final SimpleMappingConfiguration to, final MappingPart a) {
        final DynamicMappingPart result = new DynamicMappingPart(to, stablePartGeneration || a.isStable());
        result.setInput(a.getInput());
        result.setOutput(a.getInput());
        return result;
    }

    @Override
    public void start() { }


    @Override
    public void end() { }

    @Override
    public String getType() {
        return MemoryMatchLink.class.getName();
    }

    public void setLinks(final List<MappingPortrayal> links) {
        this.links = links;
    }

    public void setStablePartGeneration(final boolean stablePartGeneration) {
        this.stablePartGeneration = stablePartGeneration;
    }

    public void setCreateMultipleValues(final boolean createMultipleValues) {
        this.createMultipleValues = createMultipleValues;
    }

    public void setSkipMappingPartCreationForLinks(final boolean skipMappingPartCreationForLinks) {
        this.skipMappingPartCreationForLinks = skipMappingPartCreationForLinks;
    }

    public void setRefreshOnRead(final boolean refreshOnRead) {
        this.refreshOnRead = refreshOnRead;
    }

    public void setUsingExistingRegistry(final String usingExistingRegistry) {
        this.usingExistingRegistry = usingExistingRegistry;
    }

    public void setIndexer(final List<IndexAltering> indexAltering) {
        this.indexAltering = indexAltering;
    }
}