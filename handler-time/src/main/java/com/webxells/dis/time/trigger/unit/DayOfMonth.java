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

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidApi;
import java.time.LocalDateTime;
import java.util.List;

@Description("Triggers every month at given day; times format [1-31]")
public class DayOfMonth extends IntUnit {

    public DayOfMonth(final int value, final List<TimeUnit> morePrecise) {
        super(value, morePrecise);
    }

    public DayOfMonth() {
    }

    @Override
    public void validate() throws InvalidApi {
        if (31 < value || 1 > value) {
            throw new InvalidApi("Invalid day of year");
        }
    }

    @Override
    public LocalDateTime calcNextRunForCurrent(final LocalDateTime now) {
        return tryValueOrLower(now::withDayOfMonth);
    }

    @Override
    public LocalDateTime increase(final LocalDateTime now) {
        return now.plusMonths(1);
    }
}