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

public class TestStaticTrigger extends SimpleConcurrentTrigger<TestTriggerConfig> {
    private static boolean shouldTrigger;
    private ConfigurableByType config;

    public static void execute() {
        shouldTrigger = true;
    }

    public TestStaticTrigger(SimpleConcurrentTriggerConfig config) {
        super(config);
        shouldTrigger = false;
        this.config = config;
    }

    @Override
    protected boolean shouldTrigger() {
        return shouldTrigger;
    }
}