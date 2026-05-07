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


import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.base.trigger.LimiterConfig;
import java.time.temporal.ChronoUnit;

@Description("Delays another trigger by specified time")
public class TriggerDelayConfig extends LimiterConfig {
    @Default("0")
    @Required
    private long delay;
    @Default("SECONDS")
    @Required
    private ChronoUnit unit = ChronoUnit.SECONDS;

    public long getDelay() {
        return delay;
    }

    public void setDelay(final long delay) {
        this.delay = delay;
    }

    public ChronoUnit getUnit() {
        return unit;
    }

    public void setUnit(final ChronoUnit unit) {
        this.unit = unit;
    }

    @Override
    public String getType() {
        return TriggerDelay.class.getName();
    }
}