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
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import java.util.List;

@Description("Moves values of this Dataset to the SubData of the given MappingPart")
public class ValuesToSubConfiguration implements SingleCallForAllValuesManipulator {
    @Required
    @Description("Where to move the values to")
    private MappingPortrayal destinyPortrayal;

    @Override
    public void validate() throws InvalidApi {
        if (null == destinyPortrayal) {
            throw new InvalidApi("destinyPortrayal is required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final List<MappingConfiguration> subConfigurations = mappingPart.getSubData();
        if (subConfigurations.size() > 1) {
            subConfigurations.retainAll(subConfigurations.subList(0, 1));
        }
        if (subConfigurations.size() > 0) {
            subConfigurations.get(0).clear();
        }
        valuesToSubConfiguration(mappingPart, subConfigurations);
        mappingPart.getDataset().clear();
    }

    public void setDestinyPortrayal(final MappingPortrayal destinyPortrayal) {
        this.destinyPortrayal = destinyPortrayal;
    }

    private void valuesToSubConfiguration(final MappingPart part, final List<MappingConfiguration> subConfigurations) {
        final List<DatasetPiece> content = part.getDataset().getContent();
        for (int i = 0, m = content.size(); i < m; i++) {
            if (subConfigurations.size() <= i) {
                subConfigurations.add( 0 == i ? createNewConfiguration(part.getConfiguration()) :
                        getCopy(subConfigurations.get(0)));
            }
            assignToExistentConfiguration(content.get(i), subConfigurations.get(i));
        }
    }

    private MappingConfiguration getCopy(final MappingConfiguration source) {
        final MappingConfiguration result = source.copy();
        result.clear();
        return result;
    }

    private MappingPoint createMappingPoint() {
        return new SimpleMappingPoint(destinyPortrayal.getReference(), destinyPortrayal.getPath());
    }

    private MappingConfiguration createNewConfiguration(final MappingConfiguration parent) {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        result.setParent(parent);
        result.addPart(createMappingPart(result));
        return result;
    }

    private MappingPart createMappingPart(final MappingConfiguration result) {
        final MappingPoint point = createMappingPoint();
        return new SimpleMappingPart(result, point, point);
    }

    private MappingPart createMappingPartWithValue(final DatasetPiece datasetPiece, final MappingConfiguration mappingConfiguration) {
        final MappingPart result = createMappingPart(mappingConfiguration);
        result.getDataset().collect(createValue(datasetPiece));
        return result;
    }

    private DatasetPiece createValue(final DatasetPiece datasetPiece) {
        return new SimpleDatasetPiece(datasetPiece.value().orElse(null));
    }

    private void assignToExistentConfiguration(final DatasetPiece datasetPiece, final MappingConfiguration mappingConfiguration) {
        mappingConfiguration.getByPortrayal(destinyPortrayal).ifPresentOrElse(
                a -> a.getDataset().collect(createValue(datasetPiece)),
                () -> mappingConfiguration.parts().add(createMappingPartWithValue(datasetPiece, mappingConfiguration)));
    }
}
