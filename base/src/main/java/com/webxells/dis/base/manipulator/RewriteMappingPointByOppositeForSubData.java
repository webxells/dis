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
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;

@Description("Overwrites input location by output location or vice versa")
public class RewriteMappingPointByOppositeForSubData implements SingleCallForAllValuesManipulator {
    @Description("Location to overwrite the other source")
    private MappingPortrayal.Source source;

    @Override
    public void manipulate(final DatasetPiece datasetPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        setForSubData(mappingPart);
    }

    private void setForSubData(final MappingPart mappingPart) {
        mappingPart.getSubData().stream()
                .flatMap(a -> a.parts().stream())
                .forEach(this::setRightByOpposite);
    }

    private void setRightByOpposite(final MappingPart a) {
        if (MappingPortrayal.Source.OUTPUT == source) {
            a.setInput(a.getOutput());
        } else {
            a.setOutput(a.getInput());
        }
        setForSubData(a);
    }

    public void setSource(final MappingPortrayal.Source source) {
        this.source = source;
    }
}
