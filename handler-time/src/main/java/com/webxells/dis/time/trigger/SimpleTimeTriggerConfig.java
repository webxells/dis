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
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;
import com.webxells.dis.time.intern.SimpleTimeApi;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public abstract class SimpleTimeTriggerConfig extends SimpleConcurrentTriggerConfig {
    @Description("Starts config on start")
    @Default("false")
    private boolean runOnStart = false;
    @Description("Delays first trigger, specified in milliseconds")
    @Default("No delay")
    private long startDelay;
    @Default("Zone of this machine")
    protected TimeZone zone = SimpleTimeApi.DEFAULT_ZONE;

    public TimeZone getZone() {
        return zone;
    }

    public void setZone(final String zone) {
        this.zone = TimeZone.getTimeZone(zone);
    }

    public void enableRunOnStart() {
        runOnStart = true;
    }

    public boolean runOnStart() {
        return runOnStart;
    }

    public void setRunOnStart(final boolean runOnStart) {
        this.runOnStart = runOnStart;
    }

    public long getStartDelay() {
        return startDelay;
    }

    public void setStartDelay(final long startDelay) {
        this.startDelay = startDelay;
    }
}