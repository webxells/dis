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

import com.webxells.dis.test.example.trigger.TestTrigger;
import com.webxells.dis.test.example.trigger.TestTriggerConfig;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AndConditionTest {

    @BeforeEach
    void reset() {
        TestTrigger.reset();
    }

    @Test
    void testReset() throws InterruptedException {
        AtomicInteger result = new AtomicInteger();
        AndConditionConfig config = new AndConditionConfig();
        config.setResetInterval(1);
        config.setResetUnit(ChronoUnit.SECONDS);
        config.setChildren(List.of(
                new TestTriggerConfig(),
                new TestTriggerConfig(),
                new TestTriggerConfig()
        ));
        AndCondition fixture = new AndCondition(config);
        fixture.awaitAction(result::getAndIncrement);
        TestTrigger.triggers.get(0).execute();
        TestTrigger.triggers.get(1).execute();
        Thread.sleep(1200);
        TestTrigger.triggers.get(2).execute();
        Thread.sleep(100);
        assertEquals(0, result.get());
        TestTrigger.triggers.get(0).execute();
        TestTrigger.triggers.get(1).execute();
        Thread.sleep(100);
        assertEquals(1, result.get());
        fixture.abort();
    }

    @Test
    void test() throws InterruptedException {
        AtomicInteger result = new AtomicInteger();
        AndConditionConfig config = new AndConditionConfig();
        config.setResetInterval(30);
        config.setResetUnit(ChronoUnit.SECONDS);
        config.setChildren(List.of(
                new TestTriggerConfig(),
                new TestTriggerConfig(),
                new TestTriggerConfig()
        ));
        AndCondition fixture = new AndCondition(config);
        assertFalse(fixture.isRunning());
        fixture.awaitAction(result::getAndIncrement);
        assertTrue(fixture.isRunning());
        TestTrigger.triggers.get(0).execute();
        assertEquals(0, result.get());
        TestTrigger.triggers.get(1).execute();
        assertEquals(0, result.get());
        TestTrigger.triggers.get(2).execute();
        Thread.sleep(100);
        assertEquals(1, result.get());
        Thread.sleep(200);
        assertEquals(1, result.get());
        fixture.abort();
    }

}