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
package com.webxells.dis.info.internal.config;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import java.util.Map;
import java.util.Optional;

public class KnownTypeMapping extends RootConfiguration {
    private static final String INTERFACE_PATH = "interface";
    private static final String IMPLEMENTATION_PATH = "implementation";

    private final boolean knownMappingAsMap;

    public KnownTypeMapping(final MappingPart part, final boolean createMissingMappingParts, final boolean knownMappingAsMap) {
        super(part, createMissingMappingParts);
        this.knownMappingAsMap = knownMappingAsMap;
    }

    public int transform() {
        final Map<Class<?>, String> known = com.webxells.dis.boot.KnownTypeMapping.getKnown();
        if (knownMappingAsMap) {
            transformToMap(known);
        } else {
            transformToMultiSubData(known);
        }
        return known.size();
    }

    private void transformToMultiSubData(final Map<Class<?>, String> known) {
        MappingConfiguration definedSubData = part.getFirstSubData();
        for (Map.Entry<Class<?>, String> entry : known.entrySet()) {
            final String interfaze = entry.getKey().getName();
            final String implementation = entry.getValue();
            if (null == definedSubData) {
                createSubData(interfaze, implementation);
            } else {
                fillSubData(definedSubData, interfaze, implementation);
                definedSubData = null;
            }
        }
    }

    private void fillSubData(final MappingConfiguration existingSubData, final String interfaze, final String implementation) {
        fillPart(existingSubData, INTERFACE_PATH, interfaze);
        fillPart(existingSubData, IMPLEMENTATION_PATH, implementation);
    }

    private void fillPart(final MappingConfiguration subData, final String path, final String value) {
        Optional<MappingPart> part = subData.getByPortrayal(new SimpleMappingPortrayal(inputReference, path));
        if (part.isEmpty() && createMissingMappingParts) {
            part = Optional.of(new SimpleMappingPart(subData,
                    new SimpleMappingPoint(inputReference, path), new SimpleMappingPoint(outputReference, path)));
        }
        part.ifPresent(a -> a.getDataset().collect(new SimpleDatasetPiece(value)));
    }

    private void createSubData(final String interfaze, final String implementation) {
        final MappingConfiguration subData = new SimpleMappingConfiguration();
        subData.setParent(part.getConfiguration());
        subData.parts().add(createPart(subData, INTERFACE_PATH, interfaze));
        subData.parts().add(createPart(subData, IMPLEMENTATION_PATH, implementation));
        part.getSubData().add(subData);
    }

    private MappingPart createPart(final MappingConfiguration subData, final String path, final String value) {
        final MappingPart result = new SimpleMappingPart(subData,
                new SimpleMappingPoint(inputReference, path),
                new SimpleMappingPoint(outputReference, path));
        result.getDataset().collect(new SimpleDatasetPiece(value));
        return result;
    }

    private void transformToMap( final Map<Class<?>, String> known) {
        final MappingConfiguration subData = part.getFirstOrNewSubData();
        known.forEach((interfaze, implementation) -> {
            final String interfaceName = interfaze.getName();
            final MappingPart mappingPart = new SimpleMappingPart(subData,
                    new SimpleMappingPoint(inputReference, interfaceName),
                    new SimpleMappingPoint(outputReference, interfaceName));
            mappingPart.getDataset().collect(new SimpleDatasetPiece(implementation));
            subData.parts().add(mappingPart);
        });
    }
}