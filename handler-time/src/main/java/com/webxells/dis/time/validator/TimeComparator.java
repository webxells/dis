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
package com.webxells.dis.time.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.time.TimeMappingPortrayal;
import com.webxells.dis.time.TimePortrayal;
import com.webxells.dis.time.intern.ZonedDateTimeOperation;
import java.time.ZonedDateTime;
import java.util.function.BiFunction;

@Description("Checks if one timestamps is before, after or the exact same as another timestamp")
public class TimeComparator extends ZonedDateTimeOperation implements Validator {
    @Description("Uses timestamp of another TimePortrayal")
    private TimePortrayal referencePoint;
    @Required
    @Description("TimePortrayal that contains timestamp to compare with")
    private TimePortrayal comparativePoint;
    @Required
    @Description("Defines how the timestamps will be compared")
    private Comparator comparator;

    @Override
    public void validate() throws InvalidApi {
        if (null == comparativePoint || null == comparator) {
            throw new InvalidApi("required fields missing");
        }
        comparativePoint.validate();
        if (null != referencePoint) {
            referencePoint.validate();
        }
    }

    @Override
    public boolean validate(final DatasetPiece datasetPiece, final MappingPart mappingPart) {
        return comparator.apply(getReference(datasetPiece, mappingPart, referencePoint)
                ,createLocalDateTimeByPoint(comparativePoint,mappingPart));
    }

    public void setReferencePoint(final TimeMappingPortrayal referencePoint) {
        this.referencePoint = referencePoint;
    }

    public void setComparativePoint(final TimeMappingPortrayal comparativePoint) {
        this.comparativePoint = comparativePoint;
    }

    public void setComparator(final Comparator comparator) {
        this.comparator = comparator;
    }

    public enum Comparator {
        AFTER(ZonedDateTime::isAfter),
        EQUALS(ZonedDateTime::isEqual),
        BEFORE(ZonedDateTime::isBefore);

        private final BiFunction<ZonedDateTime, ZonedDateTime, Boolean> comparingFunction;

        Comparator(BiFunction<ZonedDateTime, ZonedDateTime, Boolean> comparingFunction) {
            this.comparingFunction = comparingFunction;
        }

        public boolean apply(final ZonedDateTime reference, final ZonedDateTime comparative) {
            return comparingFunction.apply(reference, comparative);
        }
    }
}