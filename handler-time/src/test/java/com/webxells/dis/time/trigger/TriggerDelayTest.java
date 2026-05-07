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

import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TriggerDelayTest {
    private static boolean shouldTrigger = false;
    private static boolean triggered = false;

    public static class TestTrigger extends SimpleConcurrentTrigger<SimpleConcurrentTriggerConfig> {
        public TestTrigger(final SimpleConcurrentTriggerConfig ignore) {
            super(ignore);
        }

        @Override
        protected boolean shouldTrigger() {
            return shouldTrigger;
        }
    }

    @Test
    void test() throws InterruptedException {
        final TriggerDelayConfig config = new TriggerDelayConfig();
        config.setDelay(500);
        config.setUnit(ChronoUnit.MILLIS);
        config.setChild(new SimpleConcurrentTriggerConfig() {
            @Override
            public String getType() {
                return TestTrigger.class.getName();
            }
        });
        TriggerDelay fixture = new TriggerDelay(config);
        Assertions.assertFalse(shouldTrigger);
        Assertions.assertFalse(fixture.isRunning());
        Assertions.assertFalse(triggered);
        fixture.awaitAction(() -> triggered = true);
        Assertions.assertTrue(fixture.isRunning());
        Assertions.assertFalse(triggered);
        shouldTrigger = true;
        Thread.sleep(300);
        shouldTrigger = false;
        Assertions.assertFalse(triggered);
        Thread.sleep(400);
        Assertions.assertTrue(triggered);
        Assertions.assertTrue(fixture.isRunning());
        fixture.abort();
        Assertions.assertFalse(fixture.isRunning());
    }

}