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
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.Manipulator;

@Description("Copies current values to another MappingPart")
public class CopyToOtherPart implements Manipulator {
    @Required
    @Description("Mapping part to copy values to")
    private MappingPortrayal other;

    @Override
    public void validate() throws InvalidApi {
        if (other == null) {
            throw new InvalidApi("other is required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        mappingPart.getConfiguration().getByPortrayal(other)
                .ifPresent(a -> a.getDataset().collect(currentPiece));
    }

    public void setOther(final MappingPortrayal other) {
        this.other = other;
    }
}
