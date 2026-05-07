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
package com.webxells.dis.time.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.time.intern.SimpleTimeApi;
import java.time.LocalDateTime;

@Description("Get the current time or the last time this Manipulator was called")
public class GetDateTime extends SimpleTimeApi implements SingleCallForAllValuesManipulator {
    @Description("Get the current time")
    @Default("false")
    private boolean liveUpdate;
    private LocalDateTime savedTime;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        mappingPart.getDataset().clear();
        mappingPart.getDataset().collect(new SimpleDatasetPiece(getTime().format(format)));
    }

    private LocalDateTime getTime() {
        if (liveUpdate) {
            return LocalDateTime.now(zone.toZoneId());
        }
        if (null == savedTime) {
            savedTime = LocalDateTime.now(zone.toZoneId());
        }
        return savedTime;
    }

    public void setLiveUpdate(final boolean liveUpdate) {
        this.liveUpdate = liveUpdate;
    }
}
