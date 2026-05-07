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
package com.webxells.dis.base.trigger.group;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.trigger.Trigger;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class GroupingTrigger implements Trigger<GroupingTriggerConfiguration> {
    private static final Map<String, GroupingTriggerDirector> INSTANCES = new ConcurrentHashMap<>();

    private final GroupingTriggerDirector director;
    private final String name;

    public GroupingTrigger(final GroupingTriggerConfiguration config) {
        director = INSTANCES.computeIfAbsent(config.getGroup(), GroupingTriggerDirector::new);
        name = config.getName();
        Optional.ofNullable(config.getStart())
                .ifPresent(director::setStarterConfig);
        Optional.ofNullable(config.getRuntimeEnvironment())
                .ifPresent(director::setRuntimeEnvironment);
        Optional.of(config.getMaxParallel())
                .filter(a -> 1 < a)
                .ifPresent(director::setMaxParallel);
        Optional.of(config.getSleepInterval())
                .filter(a -> 50 < a)
                .ifPresent(director::setPauseBetweenRuns);
        Optional.ofNullable(config.getErrorStrategy())
                .ifPresent(director::setErrorStrategy);
        Optional.ofNullable(config.getStatusUpdateOutput())
                .ifPresent(a -> {
                    final UpdateSender updateSender = new UpdateSender(a, config.getGroup());
                    Optional.ofNullable(config.getStatusUpdateDateFormat())
                            .ifPresent(updateSender::setDateFormat);
                    director.setUpdateSender(updateSender, config.getEventsToUpdate());
                });
    }

    @Override
    public void validate() throws InvalidApi {
        director.validate();
    }

    @Override
    public void awaitAction(final Runnable action) {
        director.clear();
        director.register(this, action);
    }

    @Override
    public void abort() {
        director.abort();
    }

    @Override
    public boolean isRunning() {
        return director.isRunning();
    }

    String getName() {
        return name;
    }
}