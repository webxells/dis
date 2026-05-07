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
package com.webxells.dis.workflow.service.trigger;

import com.webxells.dis.api.Logger;
import com.webxells.dis.base.trigger.SimpleConcurrentTrigger;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.workflow.service.event.JobEnded;
import java.util.concurrent.atomic.AtomicInteger;

public class OnJobEnd extends SimpleConcurrentTrigger<OnJobEndConfig> {
    private final static Logger LOGGER = LoggerProxyFactory.logger(OnJobEnd.class);

    private final String jobName;
    private final EventManager.ContextProxy eventManager;
    private final AtomicInteger count = new AtomicInteger();

    public OnJobEnd(final OnJobEndConfig config) {
        super(config);
        this.jobName = config.getJobName();
        eventManager = EventManager.instance().new ContextProxy(WorkflowSecurity.instance().getJob(jobName));
    }

    private void trigger(final JobEnded event) {
        LOGGER.debug("Monitored job ended");
        count.getAndIncrement();
    }

    @Override
    public void abort() {
        super.abort();
        eventManager.reset(JobEnded.class);
    }

    @Override
    protected void start() {
        super.start();
        count.set(0);
        eventManager.on(JobEnded.class, this::trigger);
    }

    @Override
    protected boolean shouldTrigger() {
        if (count.get() > 0) {
            LOGGER.debug("Triggered ".concat(jobName));
            count.decrementAndGet();
            return true;
        }
        try {
            Thread.sleep(500);
        } catch (InterruptedException ignored) { }
        return false;
    }
}