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
package com.webxells.dis.base.output;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.output.FilteredOutputConfig.FilterEntry;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class FilteredOutput implements Output<FilteredOutputConfig> {
    private final static Logger LOGGER = LoggerProxyFactory.logger(FilteredOutput.class);

    private final FilteredOutputConfig configuration;
    private final Map<FilterEntry, Output<?>> filterEntries = new HashMap<>();


    public FilteredOutput(final FilteredOutputConfig configuration) {
        this.configuration = configuration;
        Optional.ofNullable(configuration.getFilterEntries())
                .ifPresent(a -> a.forEach(b -> filterEntries.put(b, null)));
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == configuration.getFilterEntries() ||
                configuration.getFilterEntries().stream().anyMatch(a -> null == a.output)) {
            throw new InvalidApi("Some outputs missing");
        }
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        for (final FilterEntry current : filterEntries.keySet()) {
            if (filterMatched(current, to)) {
                try {
                    final Output<?> output = getOutput(current);
                    LOGGER.debug(String.format("Filter[%s] matched for %s", getName(), output.toString()));
                    output.write(to);
                } catch (final DisException e) {
                    throw new InputOutputError("Could not start output", e);
                }
            }
        }
    }

    protected Output<?> getOutput(final FilterEntry current) throws DisException {
        if (null == filterEntries.get(current)) {
            final Output<?> output = ServiceManager.loadByConfig(current.output);
            filterEntries.put(current, output);
            output.start();
        }
        return filterEntries.get(current);
    }

    protected boolean filterMatched(final FilterEntry current, final MappingConfiguration to) {
        if (null != current.validator) {
            final Optional<MappingPart> part = to.getByPortrayal(current.portrayal);
            if (part.isPresent()) {
                final List<DatasetPiece> content = part.get().getDataset().getContent();
                for (DatasetPiece piece : content.size() > 0 ? content : List.of(new SimpleDatasetPiece(null))) {
                    if (!current.validator.validate(piece, part.get())) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void start() throws DisException { }

    @Override
    public void end() throws DisException { }
}