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
import com.webxells.dis.base.trigger.SimpleLimiter;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class TriggerDelay extends SimpleLimiter<TriggerDelayConfig> {
    private final static Logger LOGGER = LoggerProxyFactory.logger(TriggerDelay.class);

    private final TriggerDelayConfig config;

    public TriggerDelay(final TriggerDelayConfig config) {
        super(config);
        this.config = config;
    }

    @Override
    protected boolean passLimitation() {
        try {
            LOGGER.debug(String.format("Triggered! But waiting for %d %s", config.getDelay(), config.getUnit()));
            Thread.sleep(LocalDateTime.now().until(
                    LocalDateTime.now().plus(config.getDelay(), config.getUnit()), ChronoUnit.MILLIS));
        } catch (InterruptedException e) {
            throw new RuntimeException("Nightmare!", e);
        }
        return true;
    }
}
