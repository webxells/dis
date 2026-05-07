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
package com.webxells.dis.fileregistry.internal;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.fileregistry.MurMur3Strategy;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class Murmur3HashFileRegistry  {
    protected final static String LINE_SEPARATOR = System.lineSeparator();
    protected final static String PLACEHOLDER_NULL = "{{NULL}}";

    @Description("Path to file registry")
    @Required
    protected String registryDirectory;
    @Description("List of parts that are taken to generate hash")
    @Required
    protected List<MappingPortrayal> hashFields;
    @Default("Double hashing")
    protected MurMur3Strategy hashStrategy = MurMur3Strategy.DOUBLE;
    @Default("10666")
    @Description("Required to generate hash - pick a random number")
    protected int seed = 10666;

    public void validate() throws InvalidApi {
        final File directory = new File(registryDirectory);
        if (!(directory.exists() && directory.isDirectory() && directory.canWrite() && directory.canRead())) {
            throw new InvalidApi("Invalid directory provided");
        }
    }

    public void setHashFields(final List<MappingPortrayal> hashFields) {
        this.hashFields = hashFields;
    }

    public void setHashStrategy(final MurMur3Strategy hashStrategy) {
        this.hashStrategy = hashStrategy;
    }

    public void setSeed(final int seed) {
        this.seed = seed;
    }

    public void setRegistryDirectory(final String registryDirectory) {
        this.registryDirectory = registryDirectory;
    }

    protected String createDataHash(final MappingConfiguration mappingConfiguration) {
        return createDataHash(mappingConfiguration, "");
    }

    protected String createDataHash(final MappingConfiguration mappingConfiguration, final String extra) {
        return hashStrategy.createHash(mappingConfiguration.parts().stream()
                .map((MappingPart mappingPart) -> partToHashPattern(mappingPart, extra))
                .collect(Collectors.joining()), seed);
    }

    protected String partToHashPattern(final MappingPart mappingPart, final String extra) {
        return Stream.concat(
                mappingPart.getSubData().stream()
                    .map(mappingConfiguration -> createDataHash(mappingConfiguration, extra.concat("sub"))),
                mappingPart.getDataset().getContent().stream()
                    .map(a -> pieceToHashPattern(a, mappingPart.getInput(), mappingPart.getOutput()))
        ).collect(Collectors.joining(extra)).concat(extra);
    }

    protected File createFile(final String fileName) {
        return new File(String.format("%s%s%s", registryDirectory, File.separator, fileName));
    }

    protected String createFileName(final MappingPart mappingPart) {
        return hashStrategy.createHash(
                Stream.concat(
                        createListForNameGenerating(mappingPart).stream(),
                        getAdditionalIdPaths(mappingPart.getConfiguration()).stream())
                .map(a -> pieceToHashPattern(a, mappingPart.getInput(), mappingPart.getOutput()))
                .collect(Collectors.joining()), seed);
    }

    protected String pieceToHashPattern(final DatasetPiece datasetPiece, final MappingPoint input,
                                        final MappingPoint output) {
        return pieceToHashPattern(datasetPiece.value().orElse(""), input, output);
    }

    protected String pieceToHashPattern(final String data, final MappingPoint input,
                                        final MappingPoint output) {
        return String.format("%s-%s-%s", mappingPointToHashPattern(input), mappingPointToHashPattern(output), data);
    }

    protected String mappingPointToHashPattern(final MappingPoint point) {
        if (null == point) {
            return PLACEHOLDER_NULL;
        }
        return String.format("%s-%s", Objects.requireNonNullElse(point.getPath(), PLACEHOLDER_NULL),
                Objects.requireNonNullElse(point.getReference(), PLACEHOLDER_NULL));
    }

    protected List<String> createListForNameGenerating(final MappingPart mappingPart) {
        final List<String> result = new LinkedList<>();
        result.add(mappingPart.value().orElse(PLACEHOLDER_NULL));
        result.addAll(mappingPart.getSubData().stream()
                .map(this::createDataHash)
                .collect(Collectors.toList()));
        return result;
    }

    protected List<String> getAdditionalIdPaths(final MappingConfiguration configuration) {
        return null == hashFields ? Collections.emptyList() :
                hashFields.stream()
                        .map(configuration::getByPortrayal)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .flatMap((MappingPart mappingPart) -> createListForNameGenerating(mappingPart).stream())
                        .collect(Collectors.toList());
    }

}