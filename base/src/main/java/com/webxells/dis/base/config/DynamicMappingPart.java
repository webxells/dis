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
package com.webxells.dis.base.config;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

@Description("Dynamic (stable/unstable) mapping part")
public class DynamicMappingPart extends SimpleMappingPart {
    @Description("If set, this MappingPart will not be removed, when the corresponding configuration is cleared")
    @Default("false")
    private boolean stable;

    public DynamicMappingPart(final MappingConfiguration configuration) {
        super(configuration);
    }

    public DynamicMappingPart(final MappingConfiguration configuration, MappingPart mappingPart) {
        super(configuration, mappingPart.getInput(), mappingPart.getOutput());
        stable = mappingPart.isStable();
    }

    public DynamicMappingPart(final MappingConfiguration configuration, final boolean stable) {
        super(configuration);
        this.stable = stable;
    }

    public DynamicMappingPart(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    public void setStable(final boolean stable) {
        this.stable = stable;
    }

    @Override
    public boolean isStable() {
        return stable;
    }

    @Override
    protected MappingPart getACopy(final MappingConfiguration configuration) {
        final DynamicMappingPart result = new DynamicMappingPart(configuration);
        result.setStable(stable);
        return result;
    }
}