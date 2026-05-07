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

public class RawThreadEnvironment implements RuntimeEnvironment {
    public static class RawRun extends SimpleRun {
        public RawRun(final Runnable runnable, final String name) {
            thread = new Thread(newRun(runnable), name);
        }

        public RawRun(final Runnable runnable) {
            thread = new Thread(newRun(runnable));
        }

        @Override
        public RunState state() {
            if (RunState.ABORTED == state
                    || RunState.ERROR == state) {
                return state;
            }
            if (Thread.State.TERMINATED == thread.getState()) {
                return RunState.DONE;
            }
            return state;
        }

        @Override
        public void start() {
            if (state != RunState.INITIALIZED) {
                throw new IllegalStateException("Run is already started");
            }
            state = RunState.RUNNING;
            thread.start();
        }
    }

    private static final RawThreadEnvironment INSTANCE = new RawThreadEnvironment();

    public static RawThreadEnvironment instance() {
        return INSTANCE;
    }

    @Override
    public Run newRun(final Runnable runnable) {
        return new RawRun(runnable);
    }

    @Override
    public Run newRun(final Runnable runnable, final String name) {
        return new RawRun(runnable, name);
    }

}