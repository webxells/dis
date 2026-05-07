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
package com.webxells.dis.hash;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.hash.Engine;
import com.webxells.dis.api.hash.Manager;
import com.webxells.dis.api.hash.Task;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SimpleHashManager implements Manager {
    protected final static String PLACEHOLDER_NULL = "{{NULL}}";
    public class SimpleTask implements Task {

        @Override
        public String hash(final String data) {
            return hashStrategy.run(engine, data);
        }

        @Override
        public String hash(final InputStream data) throws IOException {
            return hashStrategy.run(engine, data);
        }

        @Override
        public String hash(final MappingPart mappingPart) {
            return hash(mappingPart, List.of());
        }

        @Override
        public String hash(final MappingPart mappingPart, final List<MappingPortrayal> additionalHashFields) {
            return hash(Stream.concat(
                            createListForNameGenerating(mappingPart).stream(),
                            getAdditionalIdPaths(mappingPart.getConfiguration(), additionalHashFields).stream())
                    .map(a -> pieceToHashPattern(a, mappingPart.getInput(), mappingPart.getOutput()))
                    .collect(Collectors.joining()));
        }

        @Override
        public String hash(final MappingConfiguration mappingConfiguration) {
            return hash(mappingConfiguration, "");
        }

        private String hash(final MappingConfiguration mappingConfiguration, final String extra) {
            return hash(mappingConfiguration.parts().stream()
                    .map((MappingPart mappingPart) -> partToHashPattern(mappingPart, extra))
                    .collect(Collectors.joining()));
        }

        private String partToHashPattern(final MappingPart mappingPart, final String extra) {
            return Stream.concat(
                    mappingPart.getSubData().stream()
                            .map(mappingConfiguration -> hash(mappingConfiguration, extra.concat("sub"))),
                    mappingPart.getDataset().getContent().stream()
                            .map(a -> pieceToHashPattern(a, mappingPart.getInput(), mappingPart.getOutput()))
            ).collect(Collectors.joining(extra)).concat(extra);
        }

        private String pieceToHashPattern(final DatasetPiece datasetPiece, final MappingPoint input,
                                            final MappingPoint output) {
            return pieceToHashPattern(datasetPiece.value().orElse(""), input, output);
        }

        private String pieceToHashPattern(final String data, final MappingPoint input,
                                            final MappingPoint output) {
            return String.format("%s-%s-%s", mappingPointToHashPattern(input), mappingPointToHashPattern(output), data);
        }

        private String mappingPointToHashPattern(final MappingPoint point) {
            if (null == point) {
                return PLACEHOLDER_NULL;
            }
            return String.format("%s-%s", Objects.requireNonNullElse(point.getPath(), PLACEHOLDER_NULL),
                    Objects.requireNonNullElse(point.getReference(), PLACEHOLDER_NULL));
        }

        private List<String> createListForNameGenerating(final MappingPart mappingPart) {
            final List<String> result = new LinkedList<>();
            result.add(mappingPart.value().orElse(PLACEHOLDER_NULL));
            result.addAll(mappingPart.getSubData().stream()
                    .map(this::hash)
                    .toList());
            return result;
        }

        private List<String> getAdditionalIdPaths(final MappingConfiguration configuration, final List<MappingPortrayal> hashFields) {
            return hashFields.stream()
                    .flatMap(a -> configuration.getByPortrayal(a).stream())
                    .flatMap((MappingPart mappingPart) -> createListForNameGenerating(mappingPart).stream())
                    .collect(Collectors.toList());
        }
    }
    private HashStrategy hashStrategy = HashStrategy.SIMPLE;
    private Engine engine;

    @Override
    public Task newTask() {
        return new SimpleTask();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == engine) {
            throw new InvalidApi("required engine missing");
        }
    }

    public void setEngine(final Engine engine) {
        this.engine = engine;
    }

    public void setHashStrategy(final HashStrategy hashStrategy) {
        this.hashStrategy = hashStrategy;
    }
}