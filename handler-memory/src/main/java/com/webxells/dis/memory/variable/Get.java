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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.memory.registry.VariableRegistry;
import java.util.List;
import java.util.Optional;

@Description("Get variable by name")
public class Get implements SingleCallForAllValuesManipulator {
    @Description("Variable name")
    @Required
    private String name;
    @Description("Default values in case no variable saved")
    @Default("Do nothing")
    private List<String> defaults;
    @Description("Raise an error if not found")
    @Default("false")
    private boolean failOnNotFound;
    @Description("Keep current dataset")
    @Default("false")
    private boolean append;
    @Description("Save to mapping part")
    @Default("Current mapping part")
    private MappingPortrayal destinationPortrayal;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (!append) {
            mappingPart.getDataset().clear();
        }
        List<String> current = VariableRegistry.get(name);
        if (null == current) {
            if (null == defaults) {
                if (failOnNotFound) {
                    throw new InvalidDatasetException("Variable " + name + " not found");
                }
                return;
            }
            current = defaults;
        }
        saveValues(current, Optional.ofNullable(destinationPortrayal)
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .orElse(mappingPart));
    }

    private void saveValues(final List<String> current, final MappingPart mappingPart) {
        current.forEach(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setDefaults(final List<String> defaults) {
        this.defaults = defaults;
    }

    public void setFailOnNotFound(final boolean failOnNotFound) {
        this.failOnNotFound = failOnNotFound;
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    public void setDestinationPortrayal(final MappingPortrayal destinationPortrayal) {
        this.destinationPortrayal = destinationPortrayal;
    }
}