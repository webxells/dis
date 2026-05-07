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
package com.webxells.dis.time.trigger;

import com.webxells.dis.api.error.InvalidApi;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class Interval extends SimpleTimeTrigger<IntervalConfig> {
    private final long interval;
    private final ChronoUnit unit;

    public Interval(final IntervalConfig config) {
        super(config);
        interval = config.getInterval();
        unit = config.getUnit();
    }

    @Override
    public void validate() throws InvalidApi {
        if (1 > interval || null == unit) {
            throw new InvalidApi("Interval must have a valid unit or a valid interval.");
        }
    }

    @Override
    protected LocalDateTime calculateNextRun(final LocalDateTime now) {
        return now.plus(interval, unit);
    }
}