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

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import java.time.temporal.ChronoUnit;

@Description("Starts config at every time interval")
public class IntervalConfig extends SimpleTimeTriggerConfig {
    @Required
    private ChronoUnit unit;
    @Required
    private long interval;

    @Override
    public String getType() {
        return Interval.class.getName();
    }

    public long getInterval() {
        return interval;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    public void setUnit(final ChronoUnit unit) {
        this.unit = unit;
    }

    public void setInterval(final long interval) {
        this.interval = interval;
    }
}