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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class OnJobEnd extends SimpleConcurrentTrigger<OnJobEndConfig> {
    private final static Logger LOGGER = LoggerProxyFactory.logger(OnJobEnd.class);

    private final static Map<String, AtomicLong> REGISTRY = new ConcurrentHashMap<>();
    private final static Map<String, AtomicLong> REGISTRY_COUNT = new ConcurrentHashMap<>();

    public static void trigger(final String name) {
        LOGGER.trace("Got job end notification: ".concat(name));
        Optional.ofNullable(REGISTRY.get(name))
                .ifPresent(a -> {
                    LOGGER.debug(String.format("Found %s for %d instances", name, a.get()));
                    a.set(REGISTRY_COUNT.get(name).get());
                });
    }

    private final String jobName;

    private static synchronized long getAndDecrease(final String jobName) {
        return REGISTRY.get(jobName).getAndUpdate(a -> a > 0 ? --a : 0);
    }

    public OnJobEnd(final OnJobEndConfig config) {
        super(config);
        this.jobName = config.getJobName();
        REGISTRY.putIfAbsent(jobName, new AtomicLong());
        REGISTRY_COUNT.putIfAbsent(jobName, new AtomicLong());
        REGISTRY_COUNT.get(jobName).getAndIncrement();
    }

    @Override
    protected boolean shouldTrigger() {
        final long result = getAndDecrease(jobName);
        if (result > 0) {
            LOGGER.debug("Trigger ".concat(jobName));
            return true;
        } else {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) { }
        }
        return false;
    }
}