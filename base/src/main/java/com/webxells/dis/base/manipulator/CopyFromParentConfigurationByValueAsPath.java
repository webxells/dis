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
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleMappingPortrayal;
import java.util.Optional;

@Description("Copies data from parent mapping part " +
        "by using the value of the current mapping part as the path to locate the parent")
public class CopyFromParentConfigurationByValueAsPath implements SingleCallForAllValuesManipulator {
    @Description("Source of the parent mapping part")
    @Default("INPUT")
    private MappingPortrayal.Source parentSource = MappingPortrayal.Source.INPUT;
    @Description("Reference of the parent mapping part")
    private String parentReference;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final Optional<String> path = mappingPart.value();
        mappingPart.getDataset().clear();
        mappingPart.getConfiguration().parent()
                .filter(a -> path.isPresent())
                .flatMap(a -> a.getByPortrayal(createPortrayal(mappingPart, path.get())))
                .ifPresent(a -> mappingPart.getDataset().collectCopy(a.getDataset()));
    }

    private MappingPortrayal createPortrayal(final MappingPart ownPart, final String path) {
        return new SimpleMappingPortrayal(parentSource,
                Optional.ofNullable(parentReference)
                        .orElse(ownPart.getMappingPointBySource(parentSource).getReference()), path);
    }

    public void setParentReference(final String parentReference) {
        this.parentReference = parentReference;
    }

    public void setParentSource(final MappingPortrayal.Source parentSource) {
        this.parentSource = parentSource;
    }
}
