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
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import java.util.List;
import java.util.Optional;

@Description("Clears data and subData of either this MappingPart or the given one")
public class ResetPart implements SingleCallForAllValuesManipulator {
    private List<MappingPortrayal> mappingPortrayals;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (null == mappingPortrayals) {
            reset(mappingPart);
        } else {
            mappingPortrayals.stream()
                    .map(a -> mappingPart.getConfiguration().getByPortrayal(a))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(this::reset);
        }
    }

    /**
     * @todo: reset
     */
    private void reset(final MappingPart mappingPart) {
        mappingPart.getDataset().clear();
        final List<MappingConfiguration> subData = mappingPart.getSubData();
        if (!subData.isEmpty()) {
            final MappingConfiguration firstSubData = mappingPart.getSubData().get(0);
            subData.retainAll(List.of(firstSubData));
            firstSubData.clear();
        }
    }

    public void setMappingPortrayals(final List<MappingPortrayal> mappingPortrayals) {
        this.mappingPortrayals = mappingPortrayals;
    }
}
