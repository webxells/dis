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
import com.webxells.dis.time.trigger.unit.TimeUnit;

@Description("Triggers at specific time (exact to second: if on next second the provided time validates, job will be triggered as well )")
public class TimeConfig extends SimpleTimeTriggerConfig {
    @Description("instant of time")
    @Required(xor = {"time", "cron"})
    private TimeUnit instant;
    @Description("Time of day in format of hh:mm")
    @Required(xor = {"instant", "cron"})
    private String time;
    @Description("Unix crontab specific format: minute hour dayOfMonth month dayOfWeek \nonly numbers and wildcard (*) is allowed \nsecond is 0")
    @Required(xor = {"time", "instant"})
    private String cron;

    @Override
    public String getType() {
        return Time.class.getName();
    }

    public TimeUnit getInstant() {
        return instant;
    }

    public void setInstant(final TimeUnit instant) {
        this.instant = instant;
    }

    public String getTime() {
        return time;
    }

    public void setTime(final String time) {
        this.time = time;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(final String cron) {
        this.cron = cron;
    }
}