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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;

@Description("Validates first value of a mapping part and determines the further behaviour (if-then-else)")
public class OverruleOnceForAll extends Overrule implements SingleCallForAllValuesManipulator {
    @Description("Retains existing data")
    @Default("false")
    private boolean append;

    @Override
    protected void clearValue(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        mappingPart.getDataset().clear();
    }

    @Override
    protected void overwrite(final String newValue, final DatasetPiece currentPiece, final MappingPart mappingPart) {
        if (append) {
            mappingPart.getDataset().collect(new SimpleDatasetPiece(newValue));
        } else {
            super.overwrite(newValue, currentPiece, mappingPart);
        }
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }
}