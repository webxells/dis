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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JustStartTest extends SimpleTestCase {
    private boolean fail;

    @Test
    void test() throws InterruptedException {
        JustStart fixture = new JustStart(new JustStart.JustStartConfiguration());

        assertTrue(fixture.shouldTrigger());
        fail = true;
        final Thread endlessSleep = new Thread(() -> {
            fixture.shouldTrigger();
            if (fail) {
                fail();
            }
        });
        endlessSleep.start();
        Thread.sleep(randomMax(1000));
        assertSame(Thread.State.TIMED_WAITING, endlessSleep.getState());
        fail = false;
        endlessSleep.interrupt();
    }

}