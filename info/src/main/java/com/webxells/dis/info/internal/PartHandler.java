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
package com.webxells.dis.info.internal;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;

public abstract class PartHandler {
    private final String inputReference;
    private final boolean createMissingMappingParts;
    private final String outputReference;

    public PartHandler(final RootConfiguration.Configuration configuration) {
        inputReference = configuration.inputReference();
        outputReference = configuration.outputReference();
        createMissingMappingParts = configuration.createMissingMappingParts();
    }


    protected List<MappingPart> getMappingParts(final MappingConfiguration mappingConfiguration, final String path) {
        final List<MappingPart> found = mappingConfiguration.getAllByPortrayal(
                new SimpleMappingPortrayal(inputReference, path));
        if (found.isEmpty() && createMissingMappingParts) {
            return List.of(createMappingPart(mappingConfiguration, path));
        }
        return found;
    }

    private MappingPart createMappingPart(final MappingConfiguration mappingConfiguration, final String path) {
        final SimpleMappingPart result = new SimpleMappingPart(mappingConfiguration);
        result.setInput(new SimpleMappingPoint(inputReference, path));
        result.setOutput(new SimpleMappingPoint(outputReference, path));
        mappingConfiguration.parts().add(result);
        return result;
    }
}