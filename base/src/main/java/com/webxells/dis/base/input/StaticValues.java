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
package com.webxells.dis.base.input;

import com.webxells.dis.api.DatasetPiece;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Description("Provides input of fixed values")
public class StaticValues implements Input<StaticValues.Config> {
    public static class Config implements InputConfig {
        @Required
        @Description("Reference of this handler")
        private String name;
        @Required
        @Description("List of \"path-key and list of values -mapping\"")
        private List<Map<String, List<String>>> values;
        @Override
        public String getType() {
            return StaticValues.class.getName();
        }

        public void setName(final String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        public void setValues(final List<Map<String, List<String>>> values) {
            this.values = values;
        }

        public List<Map<String, List<String>>> getValues() {
            return values;
        }
    }

    private final Config config;
    private Iterator<Map<String, List<String>>> iterator;

    public StaticValues(final Config config) {
        this.config = config;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        if (hasNext()) {
            final AtomicInteger count = new AtomicInteger();
            iterator.next()
                    .forEach((key, values) ->
                            getPart(from, key)
                                    .forEach(a -> {
                                        final List<DatasetPiece> pieces = getPieces(values);
                                        a.getDataset().collect(pieces);
                                        count.addAndGet(pieces.size());
                                    }));
            return count.get();
        }
        return 0;
    }

    private List<DatasetPiece> getPieces(final List<String> values) {
        return values.stream()
                .map(value -> (DatasetPiece) new SimpleDatasetPiece(value))
                .toList();
    }

    private Stream<MappingPart> getPart(final MappingConfiguration from, final String key) {
        return from.getAllByPortrayal(new SimpleMappingPortrayal(getName(), key)).stream();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        return Optional.ofNullable(iterator)
                .map(Iterator::hasNext)
                .orElse(false);
    }

    @Override
    public String getName() {
        return config.name;
    }

    @Override
    public void start() throws DisException {
        iterator = config.values.iterator();
    }

    @Override
    public void end() throws DisException {
        iterator = null;
    }
}