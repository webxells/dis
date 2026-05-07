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

import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.TestWorkflowSecurity;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowSecurityTest extends SimpleTestCase {

    @Test
    void run() throws InterruptedException {
        assertFalse(WorkflowSecurity.isSetUp());
        setUpWorkflowSecurity();
        assertTrue(WorkflowSecurity.isSetUp());
        Job job1 = Mockito.mock(Job.class);
        Job job2 = Mockito.mock(Job.class);
        String name1 = random();
        String name2 = random();
        assertThrows(IllegalStateException.class, () -> WorkflowSecurity.register(new WorkflowSecurity() {}));
        TestWorkflowSecurity.setJobs(name1, job1);
        WorkflowSecurity instance = WorkflowSecurity.instance();
        assertEquals(job1, instance.findJob(name1).get());
        Object lock = new Object();
        new Thread(() -> {
            assertTrue(instance.findJob(name1).isEmpty());
            TestWorkflowSecurity.setJobs(name2, job2);
            assertEquals(job2, instance.findJob(name2).get());
            synchronized (lock) {
                lock.notify();
            }
        }).start();
        synchronized (lock) {
            lock.wait();
        }
        assertTrue(TestWorkflowSecurity.isSet(name2));
        assertTrue(instance.findJob(name2).isEmpty());
        assertThrows(IllegalArgumentException.class, () -> instance.getJob(name2));
    }

}