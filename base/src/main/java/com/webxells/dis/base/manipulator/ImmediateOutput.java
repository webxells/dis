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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;

@Description("Simply calling Output immediately")
public class ImmediateOutput implements SingleCallForAllValuesManipulator {
    private final com.webxells.dis.base.validator.ImmediateOutput validator = new com.webxells.dis.base.validator.ImmediateOutput();
    @Required
    private OutputConfig configuration;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
            validator.setConfiguration(configuration);
            validator.validate(currentPiece, mappingPart);
    }

    public void setConfiguration(final OutputConfig configuration) {
        this.configuration = configuration;
    }
}