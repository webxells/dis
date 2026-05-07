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
package com.webxells.dis.time.trigger.unit;

import com.webxells.dis.api.error.InvalidApi;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

public abstract class IntUnit extends MorePreciseUnit {
    protected int value;

    public IntUnit(final int value, final List<TimeUnit> morePrecise) {
        this.value = value;
        this.morePrecise = morePrecise;
    }

    public IntUnit() {}

    @Override
    public void validate() throws InvalidApi {
        if (0 > value) {
            throw new InvalidApi("Negative times not allowed");
        }
    }

    public void setValue(final int value) {
        this.value = value;
    }

    protected void assertNowOrLater(final int now, final int other) {
        if (now > other) {
            throw new CalculationException("Calculated date in th past");
        }
    }

    protected LocalDateTime tryValueOrLower(final Function<Integer, LocalDateTime> createDateTime) {
        int currentValue = value;
        while (currentValue > 0) {
            try {
                return createDateTime.apply(currentValue);
            } catch (final DateTimeException e) {
                currentValue--;
            }
        }
        throw new CalculationException("Could not calculate next run");
    }
}