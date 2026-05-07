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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.boot.ServiceManager;
import java.util.List;
import java.util.Optional;

@Description("Reads input and puts it into subData")
public class ReadToSubData implements SingleCallForAllValuesManipulator {
    @Alias("input")
    private InputConfig inputConfig;
    @Description("Mapping parts that are used by the given input")
    private List<MappingPortrayal> readOnly;
    @Description("Skips check if the input has further data to read and just continues reading")
    @Default("false")
    private boolean skipHasNext;
    @Description("How many data entries should be read")
    @Default("0")
    private int limit;
    @Description("Mapping part with the amount of data entries that should be read")
    private MappingPortrayal limitPortrayal;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final List<MappingConfiguration> subData = mappingPart.getSubData();
        if (subData.isEmpty()) {
            return;
        }
        final MappingConfiguration first = subData.get(0);
        subData.clear();
        try {
            read(first, subData, mappingPart);
        } catch (final DisException e) {
            throw new InvalidDatasetException("Could not read by child", e);
        }
    }

    private void read(final MappingConfiguration first, final List<MappingConfiguration> subData, final MappingPart mappingPart) throws DisException {
        final Input<?> input = ServiceManager.loadByConfig(inputConfig);
        input.start();
        int current = 0;
        final int limit = getLimit(mappingPart);
        while(skipHasNext || input.hasNext()) {
            final MappingConfiguration readConfiguration = createReadConfiguration(first, mappingPart);
            final int readCount = input.read(readConfiguration);
            if (0 == readCount) {
                break;
            }
            subData.add(fillValues(subData.isEmpty() ? first : first.copy(), input.getName(), readConfiguration));
            if (limit > 0 && ++current == limit) {
                break;
            }
        }
        if (subData.isEmpty()) {
            subData.add(first);
        }
        input.end();
    }

    private int getLimit(final MappingPart mappingPart) {
        return Optional.ofNullable(limitPortrayal)
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .flatMap(MappingPart::value)
                .filter(a -> a.chars().allMatch(Character::isDigit))
                .map(Integer::parseInt)
                .orElse(limit);
    }

    private MappingConfiguration fillValues(final MappingConfiguration configuration, final String name, final MappingConfiguration temp) {
        configuration.clear();
        temp.partsBySource(name)
                .forEach(a -> configuration.getAllByPortrayal(SimpleMappingPortrayal.source(a)).stream()
                        .filter(b -> b.getOutput() == null && a.getOutput() == null ||
                                SimpleMappingPortrayal.destination(b).matches(a))
                        .findAny()
                        .ifPresent(b -> b.copyValues(a)));
        return configuration;
    }

    private MappingConfiguration createReadConfiguration(final MappingConfiguration first, final MappingPart mappingPart) {
        final MappingConfiguration copy = first.copy();
        copy.clear();
        Optional.ofNullable(readOnly).stream()
                .flatMap(a -> readOnly.stream())
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a).stream())
                .map(MappingPart::copy)
                .forEach(a -> copy.parts().add(a));
        return copy;
    }

    public void setInputConfig(final InputConfig inputConfig) {
        this.inputConfig = inputConfig;
    }

    public void setReadOnly(final List<MappingPortrayal> readOnly) {
        this.readOnly = readOnly;
    }

    public void setSkipHasNext(final boolean skipHasNext) {
        this.skipHasNext = skipHasNext;
    }

    public void setLimit(final int limit) {
        this.limit = limit;
    }

    public void setLimitPortrayal(final MappingPortrayal limitPortrayal) {
        this.limitPortrayal = limitPortrayal;
    }
}