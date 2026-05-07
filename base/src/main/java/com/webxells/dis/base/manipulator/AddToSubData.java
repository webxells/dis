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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Adds data to the sub data of the current mapping part or another one")
public class AddToSubData implements SingleCallForAllValuesManipulator {
    @Description("Holds fixed values and there designated mapping part")
    public static class StaticValues {
        @Required
        public MappingPortrayal destination;
        @Required
        public List<String> data;

        private boolean isErroneous() {
            return null == destination || null == data;
        }
    }

    @Description("Holds values from a mapping part and there designated new mapping part")
    public static class MapData {
        @Required
        public MappingPortrayal destination;
        @Required
        public MappingPortrayal data;

        private boolean isErroneous() {
            return null == data || null == destination;
        }
    }

    public static class SubDataRow {
        public List<StaticValues> staticValues;
        public List<MapData> map;
    }

    @Required
    @Description("Defines data to add")
    private List<SubDataRow> add;
    @Description("Mapping part whose sub data will be extended; if not set, the current one will be used")
    private MappingPortrayal field;

    @Override
    public void validate() throws InvalidApi {
        if (null == add || add.stream().anyMatch(this::isErroneous)) {
            throw new InvalidApi("required fields missing");
        }
    }

    private boolean isErroneous(final SubDataRow subDataRow) {
        if (null != subDataRow.staticValues && subDataRow.staticValues.stream().anyMatch(StaticValues::isErroneous)) {
            return true;
        }
        if (null != subDataRow.map && subDataRow.map.stream().anyMatch(MapData::isErroneous)) {
            return true;
        }
        return null == subDataRow.staticValues && null == subDataRow.map;
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        getMappingPart(mappingPart).ifPresent(part -> {
            for (final SubDataRow subDataRow : add) {
                final MappingConfiguration subData = getSubData(part);
                assignStaticValues(subData, subDataRow.staticValues);
                assignMapData(subData, subDataRow.map, part.getConfiguration());
            }
        });
    }

    private Optional<MappingPart> getMappingPart(final MappingPart mappingPart) {
        return Optional.ofNullable(field)
                .map(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .orElse(Optional.of(mappingPart));
    }

    public void setAdd(final List<SubDataRow> add) {
        this.add = add;
    }

    public void setField(final MappingPortrayal field) {
        this.field = field;
    }

    private void assignMapData(final MappingConfiguration subData, final List<MapData> map, final MappingConfiguration configuration) {
        if (null == map) {
            return;
        }
        map.forEach(mapData -> subData.getByPortrayal(mapData.destination)
                .ifPresent(a -> a.getDataset().collect(
                        configuration.getByPortrayal(mapData.data).stream()
                                .flatMap(b -> b.getDataset().getContent().stream())
                                .collect(Collectors.toList())
                )));
    }

    private void assignStaticValues(final MappingConfiguration subData, final List<StaticValues> staticValues) {
        if (null == staticValues) {
            return;
        }
        staticValues.forEach(staticValue -> subData.getByPortrayal(staticValue.destination)
                .ifPresent(a -> a.getDataset().collect(
                        staticValue.data.stream()
                                .map(SimpleDatasetPiece::new)
                                .collect(Collectors.toList()))));
    }

    private MappingConfiguration getSubData(final MappingPart mappingPart) {
        if (1 == mappingPart.getSubData().size() && mappingPart.getSubData().get(0).parts().stream().allMatch(a -> a.value().isEmpty())) {
            return mappingPart.getSubData().get(0);
        }
        final MappingConfiguration copy = mappingPart.getSubData().get(0).copy();
        copy.clear();
        mappingPart.getSubData().add(copy);
        return copy;
    }
}
