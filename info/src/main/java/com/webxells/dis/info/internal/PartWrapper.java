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
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.List;

public class PartWrapper {
    private final MappingPart part;

    public PartWrapper(final MappingPart part) {
        this.part = part;
    }

    public MappingConfiguration getFirstSubData() {
        if (part.getSubData().isEmpty()) {
            return null;
        }
        final MappingConfiguration result = part.getSubData().getFirst();
        if (1 < part.getSubData().size()) {
            part.getSubData().retainAll(List.of(result));
        }
        result.clear();
        return result;
    }

    public MappingConfiguration getFirstOrNewSubData() {
        if (part.getSubData().isEmpty()) {
            return createBlankSubData();
        }
        return getFirstSubData();
    }

    public MappingConfiguration createBlankSubData() {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        result.setParent(part.getConfiguration());
        part.getSubData().add(result);
        return result;
    }

    public MappingConfiguration createNewSubData() {
        if (part.getSubData().isEmpty()) {
            return createBlankSubData();
        }
        return createNewStructuredData();
    }

    public MappingConfiguration getConfiguration() {
        return part.getConfiguration();
    }

    public List<MappingConfiguration> getSubData() {
        return part.getSubData();
    }

    public MappingConfiguration createNewStructuredData() {
        final MappingConfiguration result = part.getSubData().getFirst().copy();
        result.clear();
        part.getSubData().add(result);
        return result;
    }
}