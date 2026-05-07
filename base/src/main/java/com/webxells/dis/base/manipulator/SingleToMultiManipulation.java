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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import java.util.List;

@Description("Single Manipulator bearing multiple Manipulators")
public class SingleToMultiManipulation implements SingleCallForAllValuesManipulator {
    @Description("The Manipulators")
    @Required
    private List<Manipulator> children;

    @Override
    public void validate() throws InvalidApi {
        if (null == children) {
            throw new InvalidApi("children are required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        for (final Manipulator child : children) {
            for (final DatasetPiece piece : getData(mappingPart, currentPiece)) {
                child.manipulate(piece, mappingPart);
                if (child instanceof SingleCallForAllValuesManipulator) {
                    break;
                }
            }
        }
    }

    private DatasetPiece[] getData(final MappingPart mappingPart, final DatasetPiece currentPiece) {
        if (mappingPart.getDataset().getContent().size() > 0) {
            return mappingPart.getDataset().getContent().toArray(new DatasetPiece[]{});
        }
        return new DatasetPiece[] { currentPiece };
    }

    public void setChildren(final List<Manipulator> children) {
        this.children = children;
    }
}