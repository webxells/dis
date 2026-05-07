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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.trigger.LimiterConfig;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

@Description("Limits how often in a provided time a config can be started")
public class MaxCallsInTimeConfig extends LimiterConfig {
    @Required
    private long maxCalls;
    @Required
    @Description("Defines the time period during which the calls are limited")
    private long timeAmount;
    @Required
    private ChronoUnit timeUnit;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (Objects.isNull(timeUnit) || maxCalls < 1 || timeAmount < 1) {
            throw new InvalidApi("Invalid arguments provided");
        }
    }

    public long getMaxCalls() {
        return maxCalls;
    }

    public void setMaxCalls(final long maxCalls) {
        this.maxCalls = maxCalls;
    }

    public long getTimeAmount() {
        return timeAmount;
    }

    public void setTimeAmount(final long timeAmount) {
        this.timeAmount = timeAmount;
    }

    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    public void setTimeUnit(final ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    @Override
    public String getType() {
        return MaxCallsInTime.class.getName();
    }
}