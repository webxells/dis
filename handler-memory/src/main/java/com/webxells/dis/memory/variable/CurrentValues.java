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
package com.webxells.dis.memory.variable;

import com.webxells.dis.api.Dataset;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDataset;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.memory.registry.VariableRegistry;
import java.util.Optional;

@Description("MappingPart containing current variable values as dataset")
public class CurrentValues extends StableMappingPart {
    @Description("Variable name")
    @Required
    private String name;

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("name is mandatory");
        }
    }

    public CurrentValues(final MappingConfiguration configuration) {
        super(configuration);
    }

    public CurrentValues(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    @Override
    public Dataset getDataset() {
        final Dataset result = new SimpleDataset();
        VariableRegistry.getAsStream(name)
                .forEach(a -> result.collect(new SimpleDatasetPiece(a)));
        return result;
    }

    @Override
    public Optional<String> value() {
        return Optional.ofNullable(VariableRegistry.get(name))
                .flatMap(this::multiToSingle);
    }

    @Override
    public void copyValues(final MappingPart other) {
        super.copyValues(other);
        VariableRegistry.getAsStream(name)
                .forEach(a -> other.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    public void setName(final String name) {
        this.name = name;
    }
}