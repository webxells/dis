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

import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;

public abstract class RestTriggerConfig<T> extends SimpleConcurrentTriggerConfig {
    private String name;
    private int maxQueueSize = 50;
    private long waitBetweenCheck = SimpleConcurrentTrigger.DEFAULT_SLEEP_INTERVAL;
    private boolean disableQueue;
    private Transaction<T> transaction;


    public boolean isDisableQueue() {
        return disableQueue;
    }

    public void setDisableQueue(final boolean disableQueue) {
        this.disableQueue = disableQueue;
    }

    public int getMaxQueueSize() {
        return maxQueueSize;
    }

    public void setMaxQueueSize(final int maxQueueSize) {
        this.maxQueueSize = maxQueueSize;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setWaitBetweenCheck(final long waitBetweenCheck) {
        this.waitBetweenCheck = waitBetweenCheck;
    }

    public long getWaitBetweenCheck() {
        return waitBetweenCheck;
    }

    public Transaction<T> getTransaction() {
        return transaction;
    }

    public void setTransaction(final Transaction<T> transaction) {
        this.transaction = transaction;
    }
}