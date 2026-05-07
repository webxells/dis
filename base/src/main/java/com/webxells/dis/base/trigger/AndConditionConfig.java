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
package com.webxells.dis.base.trigger;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

import java.time.temporal.ChronoUnit;

@Description("Starts only if all triggers met in provided interval")
public class AndConditionConfig extends ConditionConfiguration {
    @Description("Resets triggers after interval")
    @Default("0")
    private long resetInterval;
    @Description("Time unit of the resetInterval")
    private ChronoUnit resetUnit;

    @Override
    public String getType() {
        return AndCondition.class.getName();
    }

    public long getResetInterval() {
        return resetInterval;
    }

    public void setResetInterval(final long resetInterval) {
        this.resetInterval = resetInterval;
    }

    public ChronoUnit getResetUnit() {
        return resetUnit;
    }

    public void setResetUnit(final ChronoUnit resetUnit) {
        this.resetUnit = resetUnit;
    }
}