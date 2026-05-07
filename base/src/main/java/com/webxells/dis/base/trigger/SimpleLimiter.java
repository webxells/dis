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
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Objects;

public abstract class SimpleLimiter<T extends LimiterConfig> implements Trigger<T> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(SimpleLimiter.class);

    protected Trigger<?> child;

    public SimpleLimiter(final LimiterConfig config) {
        child = ServiceManager.loadByConfig(Objects.requireNonNull(config.getChild()));
    }

    @Override
    public void awaitAction(final Runnable action) {
        child.awaitAction(() -> {
            if (passLimitation()) {
                action.run();
            } else {
                LOGGER.debug("Limit reached - trigger skipped");
            }
        });
    }

    @Override
    public void abort() {
        child.abort();
    }

    @Override
    public boolean isRunning() {
        return child.isRunning();
    }

    protected abstract boolean passLimitation();
}
