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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public abstract class SimpleTimeTrigger<T extends SimpleTimeTriggerConfig> extends SimpleConcurrentTrigger<T> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SimpleTimeTrigger.class);
    private final ZoneId zone;

    private long startDelay;
    private LocalDateTime nextRun;

    public SimpleTimeTrigger(final SimpleTimeTriggerConfig config) {
        super(config);
        zone = config.getZone().toZoneId();
        startDelay = config.getStartDelay();
        if (config.runOnStart()) {
            nextRun = now();
        }
    }

    @Override
    protected boolean shouldTrigger() {
        delayOnStart();
        final LocalDateTime now = now();
        if (null == nextRun) {
            nextRun = calculateNextRun(now);
        }
        if (timeToTrigger(now)) {
            nextRun = null;
            return true;
        }
        try {
            Thread.sleep(now.until(nextRun, ChronoUnit.MILLIS));
        } catch (final InterruptedException ignored) {
            // doesnt matter
        }
        return false;
    }

    protected LocalDateTime now() {
        return LocalDateTime.now(zone);
    }

    protected abstract LocalDateTime calculateNextRun(final LocalDateTime now);

    private void delayOnStart() {
        if (0 < startDelay) {
            LOGGER.t(String.format("Waiting %dms to start trigger", startDelay));
            try {
                Thread.sleep(startDelay);
            } catch (final InterruptedException ignored) {
                // doesnt matter
            }
            startDelay = 0;
        }
    }

    private boolean timeToTrigger(final LocalDateTime now) {
        return now.isAfter(nextRun);
    }
}