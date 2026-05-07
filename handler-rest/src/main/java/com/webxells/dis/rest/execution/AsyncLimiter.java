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
package com.webxells.dis.rest.execution;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AsyncLimiter {
    private static final Logger LOGGER = LoggerProxyFactory.logger(AsyncLimiter.class);
    private static final Map<Class<? extends AsyncLimiter>, AtomicInteger> CURRENT_WORKING_ASYNC_CALLS = new ConcurrentHashMap<>();

    private final AtomicInteger currentWorkingAsyncCalls;
    private final Request.AsyncTimer asyncTimer;

    protected  AsyncLimiter(final Request request) {
        this.asyncTimer = Optional.ofNullable(request.asyncTimer())
                .orElse(new Request.AsyncTimer(999, 500));
        currentWorkingAsyncCalls = CURRENT_WORKING_ASYNC_CALLS.computeIfAbsent(this.getClass(),
                a -> new AtomicInteger());
    }

    private void waitTillMyTurn(final Request.AsyncTimer asyncTimer) {
        while(noTimeForThis(asyncTimer.maxAsyncCalls())) {
            try {
                LOGGER.t("Reached async max calls - waiting till some are finished");
                Thread.sleep(asyncTimer.waitTimeout());
            } catch (final InterruptedException e) {
                throw new RuntimeException("Async wait interrupted", e);
            }
        }
    }

    private boolean noTimeForThis(final int max) {
        synchronized (currentWorkingAsyncCalls) {
            if (currentWorkingAsyncCalls.get() >= max) {
                return true;
            }
            currentWorkingAsyncCalls.incrementAndGet();
            return false;
        }
    }


    public ReturnPromise send(final HttpAsyncPromise httpAsyncPromise) {
        waitTillMyTurn(asyncTimer);
        return httpAsyncPromise
                .finalize(a -> currentWorkingAsyncCalls.decrementAndGet());
    }
}