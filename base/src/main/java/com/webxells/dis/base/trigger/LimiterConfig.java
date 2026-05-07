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

import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;

public abstract class LimiterConfig implements TriggerConfig {
    @Required
    protected TriggerConfig child;

    @Override
    public void validate() throws InvalidApi {
        if (null == child) {
            throw new InvalidApi("Trigger must be provided");
        }
    }

    public TriggerConfig getChild() {
        return child;
    }

    public void setChild(final TriggerConfig child) {
        this.child = child;
    }
}
