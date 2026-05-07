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
package com.webxells.dis.json.output;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.json.JsonType;
import com.webxells.dis.json.intern.Composer;
import com.webxells.dis.json.intern.ComposingJsonPath;
import com.webxells.dis.json.JsonException;
import com.webxells.dis.json.refinement.Nullable;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonOutput implements Output<JsonOutputConfig> {
    private final JsonOutputConfig configuration;
    private final Map<String, JsonType> typeMapping;
    private final Map<String, Integer> arrayIndices = new HashMap<>();
    private final Set<String> lockedArrayIndices = new HashSet<>();
    private final boolean isSingleObject;
    private final JsonOutputConfig.NoValueStrategy noValueStrategy;
    private Composer composer;

    public JsonOutput(final JsonOutputConfig configuration) {
        this.configuration = configuration;
        typeMapping = Optional.ofNullable(configuration.getTypeMapping()).orElse(Map.of());
        isSingleObject = configuration.isSingleObject();
        noValueStrategy = configuration.getNoValueStrategy();
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        arrayIndices.clear();
        lockedArrayIndices.clear();
        final List<ComposingJsonPath> paths = Stream.concat(to.partsByDestination(configuration.getName()).stream()
                .flatMap(part -> createJsonPathByOutputMappingPoint("$", part, PathConnectType.OBJECT)),
                Optional.ofNullable(configuration.getStaticMapping())
                        .map(Map::entrySet).stream()
                            .flatMap(Collection::stream)
                            .map(a -> createPartByValue(getValidStaticMappingPath(a.getKey()), a.getValue())

                ))
                .collect(Collectors.toList());
        try {
            composer.start(isSingleObject);
            composer.write(paths);
            composer.flush(isSingleObject);
        } catch (final IOException | JsonException e) {
            throw new InputOutputError("Error on json composing occured", e);
        }
    }

    private String getValidStaticMappingPath(final String path) {
        if (null == path || !(path.startsWith("$.") || configuration.isLenient())) {
            throw new IllegalArgumentException("illegal json path: " + path);
        }
        return path.startsWith("$.") ? path : "$.".concat(path);
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start() throws DisException {
        try {
            composer = new Composer(configuration.getSender().send(), configuration);
            composer.start(!isSingleObject);
        } catch (final IOException | JsonException e) {
            throw new InputOutputError("Error on starting json composer", e);
        }
    }

    @Override
    public void end() throws DisException {
        try {
            composer.close();
        } catch (final IOException e) {
            throw new InputOutputError("Error on closing composer", e);
        }
    }

    private Stream<ComposingJsonPath> createJsonPathByOutputMappingPoint(final String root, final MappingPart part,
                                                                         final PathConnectType pathConnectType) {
        final String currentPath = getValidPath(part.getOutput().getPath());
        if (null == currentPath) {
            return Stream.empty();
        }
        final String path = PathConnectType.ARRAY == pathConnectType ? calculateArrayConnectedPath(root, currentPath) :
                String.format("%s.%s", root, currentPath);
        final List<ComposingJsonPath> subData = createSubDataJsonPaths(path, part);
        return Stream.concat(
                createCurrentPartJsonPathBySubDataPresence(subData.size() > 0, path, part),
                subData.stream());
    }

    private String getValidPath(final String path) {
        if (null == path) {
            return null;
        }
        if (!(configuration.isLenient() || path.startsWith("$"))) {
            throw new IllegalArgumentException("invalid json path - only root paths allowed:" + path);
        }
        return "$".equals(path) ? "" : path.startsWith("$.") ? path.substring(2) : path;
    }

    private List<ComposingJsonPath> createSubDataJsonPaths(final String path, final MappingPart part) {
        final List<ComposingJsonPath> result = new LinkedList<>();
        for (final MappingConfiguration subDatum : part.getSubData()) {
            final int oldSize = result.size();
            lockArrayMapping(path);
            (configuration.isSubDataToReferenceRestricted() ? subDatum.partsByDestination(configuration.getName()) : subDatum.parts()).stream()
                    .flatMap(a -> createJsonPathByOutputMappingPoint(path, a, getPathConnectionType(path)))
                    .forEach(result::add);
            unlockArrayMapping(path);
            if (result.size() > oldSize) {
                getAndIncreaseArrayIndex(path);
            }
        }
        return result;
    }

    private void unlockArrayMapping(final String root) {
        lockedArrayIndices.remove(root);
    }

    private void lockArrayMapping(final String root) {
        lockedArrayIndices.add(root);
    }

    private Stream<ComposingJsonPath> createCurrentPartJsonPathBySubDataPresence(final boolean hasSubData,
                                                                                 final String path,
                                                                                 final MappingPart part) {
        if (hasSubData && part.value().isPresent()) {
            return Stream.of(createPartByDatasetPiece(calculateArrayConnectedPath(path, ""),
                    part.getDataset().getContent(), getMapping(path)));
        } else if (!hasSubData) {
            return respectNoValueStrategy(part, path);
        }
        return Stream.empty();
    }

    private Stream<ComposingJsonPath> respectNoValueStrategy(final MappingPart part, final String path)  {
        if (part.value().isEmpty() && !part.hasRefinement(Nullable.class)) {
            switch (noValueStrategy) {
                case IGNORE:
                    return Stream.empty();
                case ERROR:
                    throw new RuntimeException(String.format("value missing on part %s", part.toString()));
            }
        }
        return Stream.of(createPartByDatasetPiece(path,
                part.getDataset().getContent(), getMapping(path)));
    }

    private PathConnectType getPathConnectionType(final String currentPath) {
        final JsonType mapping = getMapping(currentPath);
        if (JsonType.OBJECT == mapping) {
            return PathConnectType.OBJECT;
        }
        return PathConnectType.ARRAY;
    }

    private String calculateArrayConnectedPath(final String root, final String current) {
        return String.format("%s[%d]%s", root, getAndIncreaseArrayIndex(root), current.isEmpty() ? "" : ".".concat(current));
    }

    private int getAndIncreaseArrayIndex(final String root) {
        int index = arrayIndices.getOrDefault(root, 0);
        if (!lockedArrayIndices.contains(root)) {
            arrayIndices.put(root, 1 + index);
        }
        return index;
    }

    private JsonType getMapping(final String path) {
        return Optional.ofNullable(typeMapping.get(path))
                .orElse(Optional.ofNullable(typeMapping.get(path.replaceAll("\\[\\d+]", "[]")))
                        .orElse(JsonType.STRING));
    }

    private ComposingJsonPath createPartByDatasetPiece(final String path,
                                                final List<DatasetPiece> content,
                                                final JsonType typeMapping) {
        return createPartByValue(path, content.stream()
                .map(a -> a.value().orElse(null))
                .collect(Collectors.toList()), typeMapping);
    }

    private ComposingJsonPath createPartByValue(final String path,
                                                final List<String> content,
                                                final JsonType typeMapping) {
        final ComposingJsonPath result = new ComposingJsonPath(path, typeMapping);
        result.setValue(content);
        return result;
    }

    private ComposingJsonPath createPartByValue(final String path, final String value) {
        return createPartByValue(path, List.of(value), getMapping(path));
    }

    private enum PathConnectType {
        OBJECT, ARRAY
    }
}