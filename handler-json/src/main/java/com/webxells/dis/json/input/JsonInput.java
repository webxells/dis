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
package com.webxells.dis.json.input;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.json.JsonException;
import com.webxells.dis.json.intern.JsonPath;
import com.webxells.dis.json.intern.Parser;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class JsonInput implements Input<JsonInputConfig> {
    private final JsonInputConfig configuration;
    private Parser jsonParser;

    public JsonInput(final JsonInputConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        if (!hasNext()) {
            return 0;
        }
        final List<JsonPath> paths = from.partsBySource(configuration.getName()).stream()
                .map(a -> JsonPath.createJsonPathByInputMappingPart(a, configuration.isLenient()))
                .collect(Collectors.toList());
        readFromParser(paths);
        final int result = paths.stream().mapToInt(this::countIt).sum();
        processUnreachablePathStrategy(paths);
        paths.forEach(this::assignResultsToMappingParts);
        return result;
    }

    private void readFromParser(final List<JsonPath> paths) throws InputOutputError {
        try {
            jsonParser.parse(List.copyOf(paths));
        } catch (final IOException | JsonException e) {
            throw new InputOutputError("An json error occured", e);
        }
    }

    private void assignResultsToMappingParts(final JsonPath jsonPath) {
        if (jsonPath.isJsonMappingPart()) {
            return;
        }
        if (null == jsonPath.getMultiMapping() && jsonPath.isValueSet()) {
            jsonPath.getMappingPartReference()
                    .ifPresent(a -> a.getDataset().collect(
                            Optional.ofNullable(jsonPath.getValue())
                                    .filter(b -> !b.isEmpty())
                                    .map(b -> b.stream().map(
                                            c -> (DatasetPiece) new SimpleDatasetPiece(c)).toList())
                                    .orElseGet(() -> List.of(new SimpleDatasetPiece(null)))));
        } else if(null != jsonPath.getMultiMapping() && jsonPath.getMappingPartReference().isPresent()) {
            assignMultiValues(jsonPath);
        }
    }

    private void assignMultiValues(final JsonPath jsonPath) {
        final MappingPart part = jsonPath.getMappingPartReference().get();
        final MappingConfiguration origConfig = part.getSubData().get(0);
        jsonPath.getMultiMapping().forEach(a -> {
            final MappingConfiguration newConfig = origConfig.copy();
            a.forEach(multiMapping -> multiMapping.getMappingPartReference()
                    .ifPresent(origMappingPart -> {
                        final int origIndex = origConfig.parts().indexOf(origMappingPart);
                        multiMapping.setMappingPartReference(newConfig.parts().get(origIndex));
                        assignResultsToMappingParts(multiMapping);
            }));
            part.getSubData().add(newConfig);
        });
        part.getSubData().remove(0);
    }

    private int countIt(final JsonPath path) {
        if (!(null == path.getMultiMapping() || path.getMultiMapping().isEmpty())) {
            return path.getMultiMapping().stream()
                    .mapToInt(a -> a.stream().mapToInt(this::countIt).sum())
                    .sum();
        }
        return path.valueSize();
    }

    private void processUnreachablePathStrategy(final Collection<JsonPath> paths) throws InputOutputError {
        for (JsonPath current : paths) {
            if (!current.isValueSet()) {
                setUnreachablePathValue(current);
            } else if (null != current.getMultiMapping()) {
                for (List<JsonPath> jsonPaths : current.getMultiMapping()) {
                    processUnreachablePathStrategy(jsonPaths);
                }
            }
        }
    }

    private void setUnreachablePathValue(final JsonPath jsonPath) throws InputOutputError {
        switch (configuration.getUnreachablePathStrategy()) {
            case FAIL:
                throw new InputOutputError("Not reachable: ".concat(jsonPath.getPath()));
            case NULL:
                jsonPath.setValue(null);
                break;
            case EMPTY:
                jsonPath.setValue(List.of(""));
                break;
            case IGNORE:
                break;
        }
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return jsonParser != null && jsonParser.hasNext();
        } catch (IOException e) {
            throw new InputOutputError("Could not peek for next", e);
        }
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void start() throws DisException {
        try {
            jsonParser = new Parser(configuration.getReceiver().receive(), configuration);
        } catch (IOException | JsonException e) {
            throw new InputOutputError("Could not initiate parser", e);
        }
    }

    @Override
    public void end() throws DisException {
        try {
            jsonParser.close();
        } catch (IOException e) {
            throw new InputOutputError("Could not terminate parser", e);
        }
    }
}