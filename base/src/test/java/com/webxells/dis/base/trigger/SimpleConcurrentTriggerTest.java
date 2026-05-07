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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SimpleConcurrentTriggerTest {
    private static boolean shouldTrigger = false;
    private static boolean triggered = false;

    public static class TestTrigger extends SimpleConcurrentTrigger<SimpleConcurrentTriggerConfig> {
        public TestTrigger(final SimpleConcurrentTriggerConfig config) {
            super(config);
        }

        @Override
        protected boolean shouldTrigger() {
            return shouldTrigger;
        }
    }

    @Test
    public void test() throws InterruptedException {
        TestTrigger fixture = new TestTrigger(new SimpleConcurrentTriggerConfig() {
            @Override
            public String getName() {
                return null;
            }
            @Override
            public void setName(final String name) { }
            @Override
            public long getSleepInterval() {
                return 0;
            }
            @Override
            public void setSleepInterval(final long sleepInterval) {}
        });
        Assertions.assertFalse(shouldTrigger);
        Assertions.assertFalse(fixture.isRunning());
        Assertions.assertFalse(triggered);
        fixture.awaitAction(() -> triggered = true);
        Assertions.assertTrue(fixture.isRunning());
        Assertions.assertFalse(triggered);
        shouldTrigger = true;
        Thread.sleep(400);
        Assertions.assertTrue(triggered);
        Assertions.assertTrue(fixture.isRunning());
        Assertions.assertThrows(Throwable.class, () -> fixture.awaitAction(()->{}));
        fixture.abort();
        Assertions.assertFalse(fixture.isRunning());
    }
}