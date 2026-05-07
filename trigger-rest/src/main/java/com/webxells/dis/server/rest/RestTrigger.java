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
package com.webxells.dis.server.rest;

import com.webxells.dis.api.Logger;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

public abstract class RestTrigger<T, R extends RestTriggerConfig<T>> extends SimpleConcurrentTrigger<R> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(RestTrigger.class);

    protected final TransactionQueue<Transaction.Entity<T>> transactionQueue;
    protected final AtomicBoolean isRunning = new AtomicBoolean();
    protected final int maxQueueSize;
    protected final boolean disableQueue;
    protected final long waitTimeout;
    protected final Transaction<T> transaction;
    protected boolean inProgress;

    public RestTrigger(final R config, final Supplier<Transaction<T>> defaultTransaction) {
        super(config);
        disableQueue = getDisableQueue(config);
        maxQueueSize = getMaxQueueSize(config);
        waitTimeout = config.getWaitBetweenCheck();
        transactionQueue = TransactionQueue.getQueue(config.getName());
        transaction = Optional.ofNullable(config.getTransaction())
                .orElseGet(defaultTransaction);
    }

    protected abstract void assertRestIsUp();
    protected abstract void stopRest();
    protected abstract boolean restRunning();

    private int getMaxQueueSize(final RestTriggerConfig<T> config) {
        return disableQueue ? 1 : config.getMaxQueueSize();
    }

    private boolean getDisableQueue(final RestTriggerConfig<T> config) {
        return config.isDisableQueue() || 1 > config.getMaxQueueSize();
    }

    protected boolean addToQueue(final T entity) {
        LOGGER.debug(String.format("New entity received: %s", entity));
        if (maxQueueSize <= transactionQueue.size() || (inProgress && disableQueue)) {
            LOGGER.i("Queue is full - entity ignored");
            return false;
        }
        transactionQueue.add(transaction.newEntity(entity));
        return true;
    }

    @Override
    protected boolean shouldTrigger() {
        assertRestIsUp();
        isRunning.set(true);
        inProgress = !transactionQueue.isEmpty();
        if (!inProgress) {
            sleep(waitTimeout);
        }
        return inProgress;
    }

    @Override
    public void abort() {
        stopRest();
        transactionQueue.reset();
        isRunning.set(false);
        super.abort();
    }

    @Override
    public boolean isRunning() {
        return isRunning.get() && restRunning() && super.isRunning();
    }
}