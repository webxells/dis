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

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;

@Description("Looks up subData that matches value of filter portrayal and " +
        "copies found data to current mapping part and its subData ")
public class FilterInSubDataCopyPart extends FilterInSubData implements SingleCallForAllValuesManipulator {

    protected void clearCurrent(final MappingPart mappingPart) {
        mappingPart.getDataset().clear();
        mappingPart.getSubData().clear();
    }

    @Override
    protected void copyValues(final MappingPart mappingPart, final MappingPart foundPart) {
        clearCurrent(mappingPart);
        foundPart.getDataset().getContent()
                .forEach(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a.value().orElse(null))));
        foundPart.getSubData().forEach(a -> mappingPart.getSubData().add(a.copy()));
    }
}