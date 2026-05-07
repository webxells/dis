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

import com.webxells.dis.api.trigger.Trigger;

public class OrCondition extends Condition<OrConditionConfiguration> {
    protected volatile boolean isRunning;

    public OrCondition(final OrConditionConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void hullabaloo(final Trigger<?> trigger) {
        if (registerRunning()) {
            action.run();
            isRunning = false;
        }
    }

    private synchronized boolean registerRunning() {
        if (!isRunning) {
            isRunning = true;
            return true;
        }
        return false;
    }

}
