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
package com.webxells.dis.workflow.service.internal;

import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.boot.WorkflowSecurity;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class ServiceSecurity extends WorkflowSecurity {
    public ServiceSecurity() {
        openSecurityContexts = new HashMap<>();
        allowedThreads = new HashMap<>();
    }

    void addJob(String name, Job job) {
        if (openSecurityContexts.containsKey(name)) {
            throw new IllegalArgumentException("Job name already used: " + name);
        }
        openSecurityContexts.put(name, job);
        addAllowance(name);
    }

    void addAllowance(String name) {
        allowedThreads.computeIfAbsent(name, a -> new LinkedList<>())
                .add(Thread.currentThread());
    }

    void reset(final String name) {
        Optional.ofNullable(allowedThreads.get(name))
                .ifPresent(List::clear);
    }
}