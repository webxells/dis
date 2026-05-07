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

abstract class SimpleRun implements RuntimeEnvironment.Run {
    protected Thread thread;
    protected volatile RuntimeEnvironment.RunState state = RuntimeEnvironment.RunState.INITIALIZED;

    @Override
    public void abort() {
        if (state != RuntimeEnvironment.RunState.RUNNING) {
            throw new IllegalStateException("Run is already started");
        }
        thread.interrupt();
        state = RuntimeEnvironment.RunState.ABORTED;
    }

    @Override
    public boolean isAlive() {
        return RuntimeEnvironment.RunState.RUNNING == state();
    }

    @Override
    public void join() throws InterruptedException {
        thread.join();
    }

    protected Runnable newRun(final Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (final Throwable e) {
                state = RuntimeEnvironment.RunState.ERROR;
                throw e;
            }
        };
    }
}