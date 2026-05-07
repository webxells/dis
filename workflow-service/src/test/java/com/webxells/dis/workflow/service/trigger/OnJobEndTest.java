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

import com.webxells.dis.base.config.SimpleJobConfig;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.TestWorkflowSecurity;
import com.webxells.dis.test.example.input.TestInputConfig;
import com.webxells.dis.workflow.service.ServiceJob;
import com.webxells.dis.workflow.service.event.JobEnded;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


class OnJobEndTest extends SimpleTestCase {
    private final AtomicInteger run = new AtomicInteger();

    @Test
    void test() throws InterruptedException {
        setUpWorkflowSecurity();
        run.set(0);
        OnJobEndConfig config1 = new OnJobEndConfig();
        config1.setJobName(random());
        OnJobEndConfig config2 = new OnJobEndConfig();
        config2.setJobName(random());

        SimpleJobConfig jobConfig1 = new SimpleJobConfig(config1.getJobName());
        jobConfig1.setTrigger(List.of());
        jobConfig1.setInput(new TestInputConfig());
        jobConfig1.setOutput(List.of());
        jobConfig1.setMapping(newConfiguration().build());
        ServiceJob serviceJob1 = Mockito.mock(ServiceJob.class);
        TestWorkflowSecurity.setJobs(jobConfig1.getName(), serviceJob1);
        SimpleJobConfig jobConfig2 = new SimpleJobConfig(config2.getJobName());
        jobConfig2.setTrigger(List.of());
        jobConfig2.setInput(new TestInputConfig());
        jobConfig2.setOutput(List.of());
        jobConfig2.setMapping(newConfiguration().build());
        ServiceJob serviceJob2 = Mockito.mock(ServiceJob.class);
        TestWorkflowSecurity.setJobs(jobConfig2.getName(), serviceJob2);
        EventManager.ContextProxy event1 = EventManager.instance().new ContextProxy(serviceJob1);
        EventManager.ContextProxy event2 = EventManager.instance().new ContextProxy(serviceJob2);


        OnJobEnd fixture1 = new OnJobEnd(config1);
        OnJobEnd fixture2 = new OnJobEnd(config1);
        OnJobEnd fixture3 = new OnJobEnd(config2);
        fixture1.awaitAction(run::getAndIncrement);
        fixture2.awaitAction(run::getAndIncrement);
        fixture3.awaitAction(run::getAndIncrement);
        assertEquals(0, run.get());
        event1.trigger(new JobEnded(serviceJob1));
        Thread.sleep(1500);
        assertEquals(2, run.get());
        Thread.sleep(600);
        assertEquals(2, run.get());
        event2.trigger(new JobEnded(serviceJob2));
        Thread.sleep(600);
        assertEquals(3, run.get());
        event1.trigger(new JobEnded(serviceJob1));
        Thread.sleep(500);
        assertEquals(5, run.get());
        fixture1.abort();
        fixture2.abort();
        fixture3.abort();
    }

}