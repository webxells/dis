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
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class WorkflowSecurity {
    protected static WorkflowSecurity instance;
    protected static Map<String, Job> openSecurityContexts;
    protected static Map<String, List<Thread>> allowedThreads;

    public static boolean isSetUp() {
        return null != instance;
    }

    public static void register(final WorkflowSecurity workflowSecurity) {
        if (null != instance) {
            throw new IllegalStateException("WorkflowSecurity already registered");
        }
        instance = workflowSecurity;
    }

    public static WorkflowSecurity instance() {
        if (null == instance || null == openSecurityContexts) {
            throw new IllegalStateException("WorkflowSecurity not initialized");
        }
        return instance;
    }

    public final Optional<Job> findJob(final String jobName) {
        return Optional.ofNullable(openSecurityContexts.get(jobName))
                .filter(a -> allowedThreads.containsKey(jobName))
                .filter(a -> allowedThreads.get(jobName).contains(Thread.currentThread()));
    }

    public final Job getJob(final String jobName) {
        return findJob(jobName)
                .orElseThrow(() -> new IllegalArgumentException("Access denied to: " + jobName));
    }

}