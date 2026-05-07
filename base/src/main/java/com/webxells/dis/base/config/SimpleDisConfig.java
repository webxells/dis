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
package com.webxells.dis.base.config;

import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.JobConfig;

import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.workflow.DisSystem;
import com.webxells.dis.api.workflow.SystemSupplier;
import java.util.LinkedList;
import java.util.List;

public class SimpleDisConfig implements DisConfig {
    @Alias("jobs")
    private List<JobConfig> configurations = new LinkedList<>();
    @Description("Defines how to run the jobs")
    private SystemSupplier<?> systemSupplier;

    public SimpleDisConfig() {}

    public void addJobConfiguration(JobConfig config) {
        configurations.add(config);
    }

    @Override
    public List<JobConfig> getConfigurations() {
        return configurations;
    }

    @Override
    public void setConfigurations(final List<JobConfig> configurations) {
        this.configurations = configurations;
    }

    @Override
    public <T extends DisSystem> SystemSupplier<T> getSystemSupplier() {
        return (SystemSupplier<T>) systemSupplier;
    }

    @Override
    public <T extends DisSystem> void setSystemSupplier(final SystemSupplier<T> system) {
        systemSupplier = system;
    }

}