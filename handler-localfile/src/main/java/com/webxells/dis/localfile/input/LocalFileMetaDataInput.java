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
package com.webxells.dis.localfile.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class LocalFileMetaDataInput implements Input<LocalFileMetaDataInputConfig> {

    public enum Attribute {
        CREATION_TIME(BasicFileAttributes::creationTime),
        LAST_MODIFIED(BasicFileAttributes::lastModifiedTime),
        LAST_ACCESS(BasicFileAttributes::lastAccessTime),
        SIZE(BasicFileAttributes::size);

        private final Function<BasicFileAttributes,Object> function;

        Attribute(final Function<BasicFileAttributes,Object> function) {
            this.function = function;
        }

        static boolean contains(final String name) {
            return Arrays.stream(values())
                    .map(Attribute::name)
                    .anyMatch(a -> a.equals(name));
        }

        Object apply(final BasicFileAttributes attributes) {
            return function.apply(attributes);
        }
    }

    private final LocalFileMetaDataInputConfig config;
    private DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public LocalFileMetaDataInput(final LocalFileMetaDataInputConfig config) {
        this.config = config;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        final AtomicInteger counter = new AtomicInteger();
        final BasicFileAttributes attributes = getAttributesFromPath();

        for (MappingPart a : from.partsBySource(config.getName())) {
            if (Objects.nonNull(a.getInput())) {
                final String attributePath = a.getInput().getPath();
                if (Attribute.contains(attributePath)) {
                    a.getDataset().collect(
                            new SimpleDatasetPiece(formatAttribute(Attribute.valueOf(attributePath).apply(attributes))));
                    counter.getAndIncrement();
                }
            }
        }
        return counter.get();
    }

    @Override
    public boolean hasNext() {
        return false;
    }

    @Override
    public String getName() {
        return config.getName();
    }

    @Override
    public void start() throws DisException {
    }

    @Override
    public void end() throws DisException {

    }

    private BasicFileAttributes getAttributesFromPath() throws InputOutputError {
        final Path path = Paths.get(config.getPath());
        try {
            return Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e) {
            throw new InputOutputError("Could not load meta data from file", e);
        }
    }

    private String formatAttribute(final Object attribute) throws InputOutputError {
        if (attribute instanceof FileTime) {
            return ((FileTime) attribute).toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                    .format(getFormatter());
        }

        return String.valueOf(attribute);
    }

    private DateTimeFormatter getFormatter() throws InputOutputError {
        if (null != config.getDateFormat()) {
            try {
                formatter = new DateTimeFormatterBuilder().appendPattern(config.getDateFormat())
                        .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
                        .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
                        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
                        .toFormatter();
            } catch(IllegalArgumentException e) {
                throw new InputOutputError("Given date format is not valid.", e);
            }

        }
        return formatter;
    }

}