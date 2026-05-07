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
package com.webxells.dis.logging.simple.pattern.path.date.symbol;

import java.time.temporal.ChronoField;

abstract class CalculateFromCurrentDateSymbol implements DateSymbol {
    private static final int ZONE_OFFSET = SimpleSymbol.ZONE_OFFSET
            .get(ChronoField.OFFSET_SECONDS) * 1000;

    private final long millisOfUnit;
    private final long maxValue;

    public CalculateFromCurrentDateSymbol(final long millisOfUnit, final long maxValue) {
        this.millisOfUnit = millisOfUnit;
        this.maxValue = maxValue;
    }

    @Override
    public String getDiffFormat(final long date) {
        final long result = (int) ((date + ZONE_OFFSET)  / millisOfUnit) % maxValue;
        return SimpleSymbol.format(result, 2);
    }
}