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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class ValueManipulation implements SingleCallForAllValuesManipulator {
    protected MappingPortrayal main;
    @Required
    protected List<MappingPortrayal> subsets;

    @Override
    public void validate() throws InvalidApi {
        if (null == subsets) {
            throw new InvalidApi("subsets required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final List<DatasetPiece> result = calculateNewValue(mappingPart);
        mappingPart.getDataset().clear();
        mappingPart.getDataset().collect(result);
    }

    protected abstract List<DatasetPiece> calculateNewValue(final MappingPart mappingPart);

    public void setMain(final MappingPortrayal main) {
        this.main = main;
    }

    public void setSubsets(final List<MappingPortrayal> subsets) {
        this.subsets = subsets;
    }

    protected List<DatasetPiece> getMainPartValues(final MappingPart mappingPart) {
        return Optional.ofNullable(main)
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .orElse(mappingPart)
                .getDataset().getContent().stream()
                .map(a -> new SimpleDatasetPiece(a.value().orElse(null)))
                .collect(Collectors.toList());
    }
}
