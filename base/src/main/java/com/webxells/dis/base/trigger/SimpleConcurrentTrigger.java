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
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.boot.RawThreadEnvironment;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class SimpleConcurrentTrigger<T extends SimpleConcurrentTriggerConfig> implements Trigger<T> {
    public static final long DEFAULT_SLEEP_INTERVAL = 500;
    private static final AtomicInteger COUNT = new AtomicInteger();
    private static final Logger LOGGER = LoggerProxyFactory.logger(SimpleConcurrentTrigger.class);
    private final RuntimeEnvironment runtimeEnvironment;

    private Runnable trigger;

    private final long sleepInterval;
    private final String name;

    private RuntimeEnvironment.Run run;
    private volatile boolean abort = false;

    public SimpleConcurrentTrigger(final SimpleConcurrentTriggerConfig config) {
        name = Optional.ofNullable(config.getName())
                .orElse(String.format("SimpleConcurrentTrigger-%s", COUNT.getAndIncrement()));
        sleepInterval = Optional.of(config.getSleepInterval())
                .filter(a -> a > 0)
                .orElse(DEFAULT_SLEEP_INTERVAL);
        runtimeEnvironment = Optional.ofNullable(config.getRuntimeEnvironment())
                .orElseGet(RawThreadEnvironment::instance);
    }

    @Override
    public void awaitAction(final Runnable trigger) {
        if (isRunning()) {
            throw new IllegalStateException("already running");
        }
        this.trigger = trigger;
        start();
    }

    @Override
    public void abort() {
        abort = true;
        try {
            if (run.isAlive()) {
                run.abort();
                run.join();
            }
        } catch (final InterruptedException ignored) { }
    }

    @Override
    public boolean isRunning() {
        return null != run && run.isAlive();
    }

    protected abstract boolean shouldTrigger();

    protected void sleep() {
        sleep(sleepInterval);
    }

    protected void sleep(final long micro) {
        if (0 < micro) {
            try {
                Thread.sleep(micro);
            } catch (final InterruptedException ignore) {
                LOGGER.t("Nightmare!");
            }
        }
    }

    protected void checkForChanges() {
        while(!abort) {
            if (shouldTrigger()) {
                trigger.run();
            }
        }
    }

    protected void start() {
        abort = false;
        run = runtimeEnvironment.newRun(this::checkForChanges, name);
        run.start();
    }
}