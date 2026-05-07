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
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Copies value from parent configuration into this SubData MappingPart")
public class CopyFromParentConfiguration implements SingleCallForAllValuesManipulator {
    @Description("Parent MappingPart")
    private MappingPortrayal source;
    private boolean skipDataset;
    private boolean skipSubData;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        mappingPart.getDataset().clear();
        mappingPart.getConfiguration().parent()
                .flatMap(a-> a.getByPortrayal(Optional.ofNullable(source).orElse(getOwnPortrayal(mappingPart))))
                .ifPresent(a -> {
                    if (!skipSubData) {
                        mappingPart.getSubData().addAll(
                                a.getSubData().stream()
                                        .map(MappingConfiguration::copy)
                                        .toList());
                    }
                    if (!skipDataset) {
                        mappingPart.getDataset().collect(
                                a.getDataset().getContent().stream()
                                        .map(b -> new SimpleDatasetPiece(b.value().orElse(null)))
                                        .collect(Collectors.toList())
                        );
                    }
                });
    }

    protected MappingPortrayal getOwnPortrayal(final MappingPart mappingPart) {
        MappingPoint point;
        MappingPortrayal.Source portrayalSource;
        if (null == mappingPart.getInput() || null == mappingPart.getInput().getPath()) {
            point =  mappingPart.getOutput();
            portrayalSource = MappingPortrayal.Source.OUTPUT;
        } else {
            point =  mappingPart.getInput();
            portrayalSource = MappingPortrayal.Source.INPUT;
        }
        return new SimpleMappingPortrayal(portrayalSource, point.getReference(), point.getPath());
    }

    public void setSource(final MappingPortrayal source) {
        this.source = source;
    }

    public void setSkipDataset(final boolean skipDataset) {
        this.skipDataset = skipDataset;
    }

    public void setSkipSubData(final boolean skipSubData) {
        this.skipSubData = skipSubData;
    }
}