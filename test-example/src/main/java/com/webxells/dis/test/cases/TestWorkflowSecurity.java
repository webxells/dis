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
package com.webxells.dis.test.cases;

import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.boot.WorkflowSecurity;
import java.util.HashMap;
import java.util.LinkedList;

public class TestWorkflowSecurity extends WorkflowSecurity {
    public TestWorkflowSecurity() {
        clear();
        openSecurityContexts = new HashMap<>();
        allowedThreads = new  HashMap<>();
    }

    public static void clear() {
        instance = null;
        openSecurityContexts = null;
        allowedThreads = null;
    }

    public static void setJobs(String name, Job job) {
        openSecurityContexts.put(name, job);
        addAllowance(name);
    }

    public static void addAllowance(String name) {
        allowedThreads.computeIfAbsent(name, a -> new LinkedList<>())
                .add(Thread.currentThread());
    }

    public static boolean isSet(String name) {
        return openSecurityContexts.containsKey(name);
    }

}