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
import com.webxells.dis.boot.ServiceManager;
import java.util.List;
import java.util.stream.Collectors;

public abstract class Condition<R extends ConditionConfiguration> implements Trigger<R> {
    protected final List<Trigger<?>> children;
    protected final R configuration;
    protected Runnable action;

    public Condition(final R configuration) {
        children = configuration.getChildren().stream()
                .map(ServiceManager::<Trigger<?>> loadByConfig)
                .collect(Collectors.toList());
        this.configuration = configuration;
    }

    @Override
    public void awaitAction(final Runnable action) {
        this.action = action;
        children.forEach(a -> a.awaitAction(() -> hullabaloo(a)));
    }

    protected abstract void hullabaloo(final Trigger<?> trigger);

    @Override
    public void abort() {
        children.forEach(Trigger::abort);
    }

    @Override
    public boolean isRunning() {
        return children.stream().allMatch(Trigger::isRunning);
    }
}
