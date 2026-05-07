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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.Optional;

@Description("Merges subData into current dataset")
public class MergeFromSubConfiguration implements SingleCallForAllValuesManipulator {
    @Description("Parent MappingPart to merge the subData")
    private MappingPortrayal rootSource;
    @Description("MappingPart of the subData that shall be merged")
    private MappingPortrayal subDataSource;
    @Description("Keeps the current content")
    @Default("false")
    private boolean keepDataset;
    @Description("Creates copies of the subDataSource Dataset")
    @Default("false")
    private boolean createDatasetPieceCopies;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        if (!keepDataset) {
            mappingPart.getDataset().clear();
        }
        mappingPart.getConfiguration().getByPortrayal(rootSource).stream()
                .flatMap(a -> a.getSubData().stream())
                .map(a -> a.getByPortrayal(subDataSource))
                .filter(Optional::isPresent)
                .flatMap(a -> a.get().getDataset().getContent().stream())
                .map(a -> createDatasetPieceCopies ? new SimpleDatasetPiece(a.value().orElse(null)) : a)
                .forEach(a -> mappingPart.getDataset().collect(a));
    }

    public void setRootSource(final MappingPortrayal rootSource) {
        this.rootSource = rootSource;
    }

    public void setSubDataSource(final MappingPortrayal subDataSource) {
        this.subDataSource = subDataSource;
    }

    public void setKeepDataset(final boolean keepDataset) {
        this.keepDataset = keepDataset;
    }

    public void setCreateDatasetPieceCopies(final boolean createDatasetPieceCopies) {
        this.createDatasetPieceCopies = createDatasetPieceCopies;
    }
}
