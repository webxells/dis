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
import com.webxells.dis.base.trigger.SimpleLimiter;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class MaxCallsInTime extends SimpleLimiter<MaxCallsInTimeConfig> {
    private MaxCallsInTimeConfig config;
    private final List<LocalDateTime> callHistory = new LinkedList<>();

    public MaxCallsInTime(final MaxCallsInTimeConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    protected boolean passLimitation() {
        callHistory.removeIf(a -> a.isBefore(calculateMinPertainDate()));
        final boolean result = config.getMaxCalls() > callHistory.size();
        if (result) {
            callHistory.add(LocalDateTime.now());
        }
        return result;
    }

    private LocalDateTime calculateMinPertainDate() {
        return LocalDateTime.now().minus(config.getTimeAmount(), config.getTimeUnit());
    }
}
