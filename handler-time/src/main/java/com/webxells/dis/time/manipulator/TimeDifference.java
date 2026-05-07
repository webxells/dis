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
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.time.TimePortrayal;
import com.webxells.dis.time.intern.SimpleTimeApi;
import com.webxells.dis.time.intern.ZonedDateTimeOperation;
import java.time.temporal.ChronoUnit;

@Description("Returns the difference in time between two timestamps")
public class TimeDifference extends ZonedDateTimeOperation implements Manipulator {
    @Description("Uses timestamp of TimePortrayal, else the current one")
    private TimePortrayal referencePoint;
    @Required
    @Description("Timestamp to compute the difference with")
    private TimePortrayal comparativePoint;
    @Description("Time unit for the comparativePoint")
    @Default("SECONDS")
    private ChronoUnit unit = SimpleTimeApi.DEFAULT_UNIT;

    @Override
    public void validate() throws InvalidApi {
        if (null == comparativePoint) {
            throw new InvalidApi("required fields are missing");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        currentPiece.rewriteValue(String.valueOf(getReference(currentPiece, mappingPart, referencePoint)
                .until(createLocalDateTimeByPoint(comparativePoint,mappingPart), unit)));
    }

    public void setReferencePoint(final TimePortrayal referencePoint) {
        this.referencePoint = referencePoint;
    }

    public void setComparativePoint(final TimePortrayal comparativePoint) {
        this.comparativePoint = comparativePoint;
    }

    public void setUnit(final ChronoUnit unit) {
        this.unit = unit;
    }
}