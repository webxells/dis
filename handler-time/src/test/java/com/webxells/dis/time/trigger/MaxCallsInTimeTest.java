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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.test.example.trigger.TestStaticTrigger;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MaxCallsInTimeTest {
    private final AtomicInteger pass = new AtomicInteger();

    @Test
    void testFailingValidation() {
        final MaxCallsInTimeConfig config = new MaxCallsInTimeConfig();
        config.setTimeUnit(ChronoUnit.SECONDS);
        config.setTimeAmount(1);
        config.setMaxCalls(1);
        config.setChild(new TestTriggerConfig(TestStaticTrigger.class.getName()));
        assertDoesNotThrow(config::validate);
        config.setMaxCalls(0);
        assertThrows(InvalidApi.class, config::validate);
        config.setMaxCalls(1);
        config.setTimeAmount(0);
        assertThrows(InvalidApi.class, config::validate);
        config.setTimeAmount(1);
        config.setTimeUnit(null);
        assertThrows(InvalidApi.class, config::validate);
        config.setTimeUnit(ChronoUnit.SECONDS);
        config.setChild(null);
        assertThrows(InvalidApi.class, config::validate);
    }

    @Test
    void test() throws InterruptedException {
        pass.set(0);
        final MaxCallsInTimeConfig config = new MaxCallsInTimeConfig();
        config.setMaxCalls(3);
        config.setTimeAmount(1);
        config.setChild(new TestTriggerConfig(TestStaticTrigger.class.getName()));
        config.setTimeUnit(ChronoUnit.SECONDS);
        MaxCallsInTime fixture = new MaxCallsInTime(config);
        fixture.awaitAction(pass::getAndIncrement);
        Assertions.assertEquals(0, pass.get());
        Assertions.assertTrue(fixture.isRunning());
        TestStaticTrigger.execute();
        Thread.sleep(200);
        Assertions.assertEquals(3, pass.get());
        Thread.sleep(1000);
        Assertions.assertEquals(6, pass.get());
        Assertions.assertTrue(fixture.isRunning());
        fixture.abort();
        Assertions.assertFalse(fixture.isRunning());
    }

}
