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

import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OnJobEndTest extends SimpleTestCase {
    private final AtomicInteger run = new AtomicInteger();

    @Test
    void test() throws InterruptedException {
        run.set(0);
        OnJobEndConfig config1 = new OnJobEndConfig();
        config1.setJobName(random("config1"));
        OnJobEndConfig config2 = new OnJobEndConfig();
        config2.setJobName(random("config2"));
        OnJobEnd fixture1 = new OnJobEnd(config1);
        OnJobEnd fixture2 = new OnJobEnd(config1);
        OnJobEnd fixture3 = new OnJobEnd(config2);
        fixture1.awaitAction(run::getAndIncrement);
        fixture2.awaitAction(run::getAndIncrement);
        fixture3.awaitAction(run::getAndIncrement);
        assertEquals(0, run.get());
        OnJobEnd.trigger(config1.getJobName());
        Thread.sleep(700);
        assertEquals(2, run.get());
        Thread.sleep(700);
        assertEquals(2, run.get());
        OnJobEnd.trigger(config2.getJobName());
        Thread.sleep(700);
        assertEquals(3, run.get());
        OnJobEnd.trigger(config1.getJobName());
        Thread.sleep(700);
        assertEquals(5, run.get());
        fixture1.abort();
        fixture2.abort();
        fixture3.abort();
    }

}