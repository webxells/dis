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
package com.webxells.dis.time.intern;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.time.TimeMappingPortrayal;
import com.webxells.dis.time.TimePortrayal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.TimeZone;

public abstract class ZonedDateTimeOperation {
    protected ZonedDateTime getReference(final DatasetPiece datasetPiece, final MappingPart mappingPart, final TimePortrayal referencePoint) {
        return null == referencePoint ?
                createLocalDateTime(
                        Optional.ofNullable(datasetPiece).flatMap(DatasetPiece::value).orElse(null),
                        TimeMappingPortrayal.DEFAULT_FORMAT, TimeMappingPortrayal.DEFAULT_ZONE) :
                createLocalDateTimeByPoint(referencePoint, mappingPart);
    }

    protected ZonedDateTime createLocalDateTime(final String value,
                                              final DateTimeFormatter dateTimeFormatter,
                                              final TimeZone timeZone) {
        return Optional.ofNullable(value)
                .map(a -> LocalDateTime.parse(a, dateTimeFormatter))
                .map(a -> a.atZone(timeZone.toZoneId()))
                .orElse(ZonedDateTime.ofLocal(LocalDateTime.MIN, timeZone.toZoneId(),
                        ZoneOffset.MIN));
    }

    protected ZonedDateTime createLocalDateTimeByPoint(final TimePortrayal point, final MappingPart mappingPart) {
        return createLocalDateTime(point.getValue(mappingPart).orElse(null), point.getFormat(), point.getZone());
    }
}