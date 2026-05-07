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
package com.webxells.dis.base.trigger.group;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.time.LocalDateTime;

import static com.webxells.dis.base.trigger.group.Child.Status.ABORTED;
import static com.webxells.dis.base.trigger.group.Child.Status.ERROR;
import static com.webxells.dis.base.trigger.group.Child.Status.INITIALIZED;

class Child {
    enum Status {
        INITIALIZED, RUNNING, ERROR, FINISHED, ABORTED
    }

    private static final Logger LOGGER = LoggerProxyFactory.logger(Child.class);

    private final GroupingTrigger trigger;
    private final Runnable toDo;

    private Status status = Status.INITIALIZED;
    private LocalDateTime start;
    private LocalDateTime end;
    private RuntimeEnvironment.Run currentRun;
    private Throwable error;

    Child(GroupingTrigger trigger, Runnable toDo) {
        this.trigger = trigger;
        this.toDo = toDo;
    }

    String getName() {
        return trigger.getName();
    }

    boolean isAlive() {
        return null != currentRun && currentRun.isAlive();
    }

    void abort() {
        if (isAlive()) {
            LOGGER.d("Aborting child %s...", getName());
            currentRun.abort();
            status = ABORTED;
        }
    }

    void startRun(final RuntimeEnvironment runtimeEnvironment, final GroupingTriggerDirector director) {
        LOGGER.d("Triggering child %s:", getName());
        currentRun = runtimeEnvironment.newRun(() -> run(director), this.getName());
        currentRun.start();
    }

    Throwable getError() {
        return error;
    }

    void reset() {
        start = end = null;
        error = null;
        currentRun = null;
        status = INITIALIZED;
    }

    private void run(final GroupingTriggerDirector director) {
        start = LocalDateTime.now();
        status = Status.RUNNING;
        try {
            toDo.run();
            status = Status.FINISHED;
        } catch (final Throwable e) {
            error = e;
            status = ERROR;
            director.handleError(this);
        }
        end = LocalDateTime.now();
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public Status getStatus() {
        return status;
    }
}