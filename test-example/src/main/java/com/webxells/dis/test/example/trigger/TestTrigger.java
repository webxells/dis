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
package com.webxells.dis.test.example.trigger;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestTrigger extends SimpleConcurrentTrigger<TestTriggerConfig> {
    public final static List<TestTrigger> triggers = new ArrayList<>();

    private AtomicBoolean shouldTrigger = new AtomicBoolean(false);
    private ConfigurableByType config;
    private String testField;
    private String type;

    public static void reset() {
        triggers.clear();
    }

    public TestTrigger(SimpleConcurrentTriggerConfig config) {
        super(config);
        triggers.add(this);
        this.config = config;
        if (config instanceof TestTriggerConfig testTriggerConfig) {
            Optional.ofNullable(testTriggerConfig.getStarter())
                    .ifPresent(a -> shouldTrigger = a);
        }
    }

    public void execute() {
        shouldTrigger.set(true);
    }

    @Override
    protected boolean shouldTrigger() {
        if (shouldTrigger.get()) {
            shouldTrigger.set(false);
            return true;
        }
        return false;
    }
}