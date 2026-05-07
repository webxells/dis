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
package com.webxells.dis.event;

import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

class EventManagerTest extends SimpleTestCase {
    static class MulchEvent implements Event {
        private final int increment;

        MulchEvent(final int increment) {
            this.increment = increment;
        }

        public int getIncrement() {
            return increment;
        }
    }

    static class MoreMulch extends MulchEvent {
        MoreMulch(final int increment) {
            super(increment);
        }
    }

    static class ParallelCount extends MulchEvent implements ConcurrentEvent {
        ParallelCount(final int increment) {
            super(increment);
        }
    }

    @Test
    void test() {
        final AtomicInteger countMulchOnThisThread = new AtomicInteger();
        final AtomicInteger countMulch = new AtomicInteger();
        final AtomicInteger countMoreMulch = new AtomicInteger();
        final Object someContext = "asd";
        final Thread testThread = Thread.currentThread();
        EventManager fixture = EventManager.instance();
        EventManager.ContextProxy contextProxy = fixture.new ContextProxy(someContext);

        fixture.on(MulchEvent.class, e -> countMulchOnThisThread.getAndAdd(e.getIncrement()));
        fixture.on(MulchEvent.class, e -> countMulch.getAndAdd(e.getIncrement()), someContext);
        fixture.on(MoreMulch.class,  e -> {
            countMoreMulch.getAndAdd(e.getIncrement());
            assertSame(testThread, Thread.currentThread());
        }, someContext);

        fixture.trigger(new MulchEvent(3), someContext);
        fixture.trigger(new MulchEvent(5), someContext);
        fixture.trigger(new MulchEvent(5), Thread.currentThread());
        fixture.trigger(new MulchEvent(7));
        fixture.trigger(new MoreMulch(12), someContext);
        fixture.trigger(new MulchEvent(18), someContext);
        fixture.reset(MulchEvent.class, someContext);
        fixture.trigger(new MulchEvent(12), someContext);
        fixture.reset(MoreMulch.class, someContext);
        fixture.redirect(someContext, List.of(Thread.currentThread()));
        contextProxy.trigger(new MulchEvent(12));
        fixture.reset(someContext);
        fixture.trigger(new MulchEvent(18), someContext);

        assertSame(someContext, contextProxy.getContext());
        assertEquals(24, countMulchOnThisThread.get());
        assertEquals(38, countMulch.get());
        assertEquals(12, countMoreMulch.get());

    }

    @Test
    void testParallel() throws InterruptedException {
        final AtomicInteger count = new AtomicInteger();
        final AtomicBoolean start = new AtomicBoolean();
        final Thread testThread = Thread.currentThread();
        int value = random(1);

        EventManager fixture = EventManager.instance();

        fixture.on(ParallelCount.class, e -> {
            while (!start.get()) {
            }
            assertNotSame(testThread, Thread.currentThread());
            count.set(e.getIncrement());
        });

        fixture.trigger(new ParallelCount(value));

        assertEquals(0, count.get());
        start.set(true);
        Thread.sleep(100);
        assertEquals(value, count.get());
    }
}