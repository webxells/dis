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
package com.webxells.dis.workflow.service;

import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.workflow.SystemSupplier;
import java.util.List;
import java.util.Map;

public class ServiceSupplier implements SystemSupplier<DisAsService> {
    @Description("Mapping of job name and other job names which events should  be accessible")
    private Map<String, List<String>> openEventContexts;
    private boolean exposeJobStatus;
    private boolean forcefullyStopJobsOnShutdown;
    private RuntimeEnvironment runtimeEnvironment;
    private long monitoringInterval;
    @Description("Max jobs running parallel")
    @Default("Count of available processors of this machine")
    private int maxParallel = Runtime.getRuntime().availableProcessors();

    @Override
    public DisAsService get(final DisConfig configuration) {
        if (null != openEventContexts) {
            assertValidNames(configuration, openEventContexts);
        }
        return new DisAsService(configuration, this);
    }

    private void assertValidNames(final DisConfig configuration, final Map<String, List<String>> openEventContexts) {
        final List<JobConfig> configurations = configuration.getConfigurations();
        openEventContexts.forEach((name, contexts) -> {
            if (!(nameExists(configurations, name)
                    && contexts.stream().allMatch(a -> nameExists(configurations, a)))) {
                throw new RuntimeException("Invalid openEventContext: " + name);
            }
        });
    }

    private boolean nameExists(final List<JobConfig> configurations, final String name) {
        return configurations.stream().anyMatch(a -> name.equals(a.getName()));
    }

    public Map<String, List<String>> getOpenEventContexts() {
        return openEventContexts;
    }

    public void setOpenEventContexts(final Map<String, List<String>> openEventContexts) {
        this.openEventContexts = openEventContexts;
    }

    public void setExposeJobStatus(final boolean exposeJobStatus) {
        this.exposeJobStatus = exposeJobStatus;
    }

    public boolean isExposeJobStatus() {
        return exposeJobStatus;
    }

    boolean shouldForcefullyStopJobsOnShutdown() {
        return forcefullyStopJobsOnShutdown;
    }

    public void setForcefullyStopJobsOnShutdown(final boolean forcefullyStopJobsOnShutdown) {
        this.forcefullyStopJobsOnShutdown = forcefullyStopJobsOnShutdown;
    }

    public int getMaxParallel() {
        return maxParallel;
    }

    public void setMaxParallel(final int maxParallel) {
        this.maxParallel = maxParallel;
    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(final RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public long getMonitoringInterval() {
        return monitoringInterval;
    }

    public void setMonitoringInterval(final long monitoringInterval) {
        this.monitoringInterval = monitoringInterval;
    }
}