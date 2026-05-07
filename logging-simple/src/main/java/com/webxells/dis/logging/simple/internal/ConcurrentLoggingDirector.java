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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.logging.simple.DisLogManager;
import com.webxells.dis.logging.simple.Event;
import java.util.Queue;

import static java.lang.Thread.State.TERMINATED;
import static java.lang.Thread.State.TIMED_WAITING;

public class ConcurrentLoggingDirector extends LoggingDirector {
    private static final int CIRCLES_AFTER_PARENT_PASSED_AWAY = 5;

    private final int waitTime;
    private final FreeConcurrentLinkedQueue<Event> queue = new FreeConcurrentLinkedQueue<>();
    private final Thread loggingThread;
    private final Thread parent = Thread.currentThread();

    private Integer parentPassedAwayCircle;

    public ConcurrentLoggingDirector(final DisLogManager disLogManager, final int waitTime) {
        super(disLogManager);
        this.waitTime = waitTime;
        loggingThread = createThread();
    }

    @Override
    void push(final Logger.LogLevel level, final String name, final String msg, final Throwable e) {
        if (!freeze) {
            queue.add(new Event(Thread.currentThread(), level, name, msg, e));
        }
    }

    private Thread createThread() {
        final Thread result = new Thread(this::tick);
        result.setDaemon(false);
        result.setPriority(Thread.MIN_PRIORITY);
        result.setName(ConcurrentLoggingDirector.class.getName());
        result.start();
        waitForThread(result);
        return result;
    }

    @SuppressWarnings("LoopConditionNotUpdatedInsideLoop")
    private void waitForThread(final Thread thread) {
        while (TIMED_WAITING != thread.getState()) {
            if (TERMINATED == thread.getState()) {
                throw new IllegalStateException("Unable to start logging thread");
            }
        }
        newLogger(getClass().getName())
                .t("Concurrent logging successfully set up");
    }

    private void tick() {
        do {
            //noinspection StatementWithEmptyBody
            while (!hold && processBatch() > 0);
            waitFotNext();
        } while(shouldTick());
    }

    private int processBatch() {
        final Queue<Event> batch = queue.getBatch();
        batch.forEach(event ->
                appenders.forEach(b -> b.write(event)));
        return batch.size();
    }

    private void waitFotNext() {
        if (waitTime > 0) {
            try {
                Thread.sleep(waitTime);
            } catch (final InterruptedException e) {
                throw new RuntimeException("Nightmare!", e);
            }
        }
    }

    private boolean shouldTick() {
        if (parent.isAlive()) {
            return true;
        }
        if (null == parentPassedAwayCircle) {
            parentPassedAwayCircle = 0;
            LoggerProxyFactory.logger(ConcurrentLoggingDirector.class).t("Dead parent found in backyard - will join it...");
        }
        return parentPassedAwayCircle++ < CIRCLES_AFTER_PARENT_PASSED_AWAY;
    }

}