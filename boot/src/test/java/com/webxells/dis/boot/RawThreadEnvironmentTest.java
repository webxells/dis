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
package com.webxells.dis.boot;

import com.webxells.dis.api.RuntimeEnvironment;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

import static com.webxells.dis.api.RuntimeEnvironment.RunState.ABORTED;
import static com.webxells.dis.api.RuntimeEnvironment.RunState.DONE;
import static com.webxells.dis.api.RuntimeEnvironment.RunState.ERROR;
import static com.webxells.dis.api.RuntimeEnvironment.RunState.INITIALIZED;
import static com.webxells.dis.api.RuntimeEnvironment.RunState.RUNNING;
import static org.junit.jupiter.api.Assertions.*;

class RawThreadEnvironmentTest {

    @Test
    void test() throws InterruptedException {
        AtomicInteger count = new AtomicInteger();
        RawThreadEnvironment environment = new RawThreadEnvironment();
        RuntimeEnvironment.Run run = environment.newRun(() -> {
            try {
                Thread.sleep(150);
            } catch (InterruptedException ignored) {
                // ignored
            }
            count.incrementAndGet();
        });
        assertEquals(INITIALIZED, run.state());
        assertFalse(run.isAlive());
        run.start();
        assertTrue(run.isAlive());
        assertEquals(0, count.get());
        assertEquals(RUNNING, run.state());
        assertTrue(run.isAlive());
        Thread.sleep(200);
        assertFalse(run.isAlive());
        assertEquals(1, count.get());
        assertEquals(DONE, run.state());
    }

    @Test
    void testAborted() {
        RawThreadEnvironment environment = RawThreadEnvironment.instance();
        RuntimeEnvironment.Run run = environment.newRun(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException ignored) {
                // ignored
            }
        });
        assertEquals(INITIALIZED, run.state());
        run.start();
        assertTrue(run.isAlive());
        assertEquals(RUNNING, run.state());
        run.abort();
        assertFalse(run.isAlive());
        assertEquals(ABORTED, run.state());
    }


    @Test
    void testError() throws InterruptedException {
        RawThreadEnvironment environment = new RawThreadEnvironment();
        RuntimeEnvironment.Run run = environment.newRun(() -> {
            throw new RuntimeException();
        }, "this exception is valid");
        assertEquals(INITIALIZED, run.state());
        run.start();
        Thread.sleep(100);
        assertFalse(run.isAlive());
        assertEquals(ERROR, run.state());
    }

}