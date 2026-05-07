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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Description("Looks up subData(s) comparing filter portrayal value with current DatasetPiece")
public class FilterInSubData implements SingleCallForAllValuesManipulator {
    @Description("Mapping part that contains subData")
    private List<MappingPortrayal> root;
    @Description("Mapping part that contains the filter value")
    private MappingPortrayal filter;
    @Description("Mapping part with value that overwrites value of filtered subData parts; " +
            "if not defined or found the data will be cleared instead")
    private MappingPortrayal result;
    @Description("Value to compare filter value for")
    @Default("current mapping parts value")
    private String value;
    @Description("Find all matching data")
    @Default("false - just first one")
    private boolean findAll;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final String compareValue = Optional.ofNullable(value)
                        .orElseGet(() -> currentPiece.value().orElse(null));
        mappingPart.getDataset().clear();
        find(getRightSubConfigurations(mappingPart).stream()
                .flatMap(a -> a.getByPortrayal(filter).stream())
                .filter(a -> a.value()
                        .filter(compare -> compare.equals(compareValue))
                        .isPresent())
                .flatMap(a -> a.getConfiguration().getByPortrayal(result).stream()))
                .forEach(a -> copyValues(mappingPart, a));
    }

    private Stream<MappingPart> find(final Stream<MappingPart> mappingPartStream) {
        return findAll ? mappingPartStream : mappingPartStream.findFirst().stream();
    }

    protected void copyValues(final MappingPart mappingPart, final MappingPart foundPart) {
        foundPart.value().ifPresent(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    private List<MappingConfiguration> getRightSubConfigurations(final MappingPart part) {
        final AtomicReference<List<MappingConfiguration>> current =
                new AtomicReference<>(List.of(part.getConfiguration()));
        root.forEach(a -> current.set(
                current.get().stream()
                    .flatMap(b -> b.getByPortrayal(a).stream())
                    .flatMap(b -> b.getSubData().stream())
                    .collect(Collectors.toList())));

        return current.get();
    }

    public void setFilter(final MappingPortrayal filter) {
        this.filter = filter;
    }

    public void setResult(final MappingPortrayal result) {
        this.result = result;
    }

    public void setRoot(final List<MappingPortrayal> root) {
        this.root = root;
    }

    public void setFindAll(final boolean findAll) {
        this.findAll = findAll;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}