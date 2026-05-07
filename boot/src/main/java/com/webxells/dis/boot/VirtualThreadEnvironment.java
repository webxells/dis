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
import java.util.Optional;

public class VirtualThreadEnvironment implements RuntimeEnvironment {
    public static class VirtualRun extends SimpleRun {
        private final String name;
        private final Runnable runnable;

        public VirtualRun(final Runnable runnable, final String name) {
            this.name = name;
            this.runnable = runnable;
        }

        public VirtualRun(final Runnable runnable) {
            this.runnable = runnable;
            name = null;
        }

        @Override
        public RunState state() {
            if (null == thread ||
                    RunState.ABORTED == state
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
            thread = Optional.ofNullable(name)
                    .map(a -> Thread.ofVirtual().name(a))
                    .orElse(Thread.ofVirtual())
                    .inheritInheritableThreadLocals(false)
                    .start(newRun(runnable));
        }

    }

    private static final VirtualThreadEnvironment INSTANCE = new VirtualThreadEnvironment();

    public static VirtualThreadEnvironment instance() {
        return INSTANCE;
    }

    @Override
    public Run newRun(final Runnable runnable) {
        return new VirtualRun(runnable);
    }

    @Override
    public Run newRun(final Runnable runnable, final String name) {
        return new VirtualRun(runnable, name);
    }

}