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
package com.webxells.dis.logging.simple.internal;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class FreeConcurrentLinkedQueue<R> {
    private static final int ABOVE_THRESHOLD_WAIT_TIME = 150;
    private static final int THRESHOLD_LOOKUP_INTERVAL = 50;

    private final AtomicStampedReference<Thread> owner =
            new AtomicStampedReference<>(null, 0);

    private int thresholdLookUp = THRESHOLD_LOOKUP_INTERVAL;
    private boolean aboveThresholdCache;
    private volatile Queue<R> current = new LinkedList<>();

    public void add(final R object) {
        asOwner(() -> current.add(object), false);
    }

    private void asOwner(final Runnable o, final boolean priority) {
        while (!priority && aboveThreshold() || !iMBoss()) {
            try {
                Thread.sleep(ABOVE_THRESHOLD_WAIT_TIME);
            } catch (final InterruptedException e) {
                throw new RuntimeException("waiting for logging failed", e);
            }
        }
        o.run();
        reset();
    }

    private boolean aboveThreshold() {
        if (--thresholdLookUp < 1) {
            thresholdLookUp = THRESHOLD_LOOKUP_INTERVAL;
            aboveThresholdCache = calculateAboveThreshold();
        }
        return aboveThresholdCache;
    }

    private boolean calculateAboveThreshold() {
        return current.size() * 500L > Runtime.getRuntime().freeMemory();
    }

    private void reset() {
        owner.set(null, 0);
    }

    private boolean iMBoss() {
        return owner.compareAndSet(null, Thread.currentThread(), 0, 1);
    }

    public synchronized Queue<R> getBatch() {
        final AtomicReference<Queue<R>> result = new AtomicReference<>();
        asOwner(() -> {
            result.set(current);
            current = new LinkedList<>();
        }, true);
        return result.get();
    }

}