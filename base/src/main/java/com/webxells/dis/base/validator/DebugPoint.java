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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Optional;

@Description("Logs information; used as debug point halting operations")
public class DebugPoint implements SingleCallForAllValuesValidator, SingleCallForAllValuesManipulator {
    private static final Logger LOGGER = LoggerProxyFactory.logger(DebugPoint.class);
    @Description("Reference of this DebugPoint")
    private String name;
    @Description("Prints information of the current MappingPart")
    @Default("false")
    private boolean printMappingPart;
    @Description("Prints whole Dataset")
    @Default("false")
    private boolean printDataset;
    @Description("Prints DatasetPiece")
    @Default("false")
    private boolean printCurrentDataset;

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        LOGGER.i("DebugPoint passed: ".concat(Optional.ofNullable(name).orElse(String.valueOf(this.hashCode()))));
        if (printMappingPart) {
            LOGGER.i("Part: ".concat(mappingPart.toString()));
        }
        if (printDataset) {
            LOGGER.i("Content: ".concat(mappingPart.getDataset().toString()));
        }
        if (printCurrentDataset) {
            LOGGER.i("Content (current): ".concat(currentPiece.value().orElse("null")));
        }
        return true;
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        validate(currentPiece, mappingPart);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setPrintDataset(final boolean printDataset) {
        this.printDataset = printDataset;
    }

    public void setPrintMappingPart(final boolean printMappingPart) {
        this.printMappingPart = printMappingPart;
    }

    public void setPrintCurrentDataset(final boolean printCurrentDataset) {
        this.printCurrentDataset = printCurrentDataset;
    }
}
