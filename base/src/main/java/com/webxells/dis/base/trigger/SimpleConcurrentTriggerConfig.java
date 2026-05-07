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

import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

public abstract class SimpleConcurrentTriggerConfig implements TriggerConfig {
    @Description("Reference of this handler")
    private String name;
    @Description("Pausing interval between Trigger validations")
    @Default("500")
    private long sleepInterval;
    private RuntimeEnvironment runtimeEnvironment;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getSleepInterval() {
        return sleepInterval;
    }

    public void setSleepInterval(final long sleepInterval) {
        this.sleepInterval = sleepInterval;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(final RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }
}