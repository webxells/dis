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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.workflow.Job;
import java.util.List;
import java.util.Optional;

public class SimpleJobConfig implements JobConfig {
    @Required
    @Description("Job name")
    private String name;
    @Required
    @Description("Conditions when this job is started")
    private List<TriggerConfig> triggers;
    @Required
    @Description("Source of the data")
    private InputConfig input;
    @Required
    @Description("Connects input with output")
    private MappingConfiguration mappings;
    @Required
    @Description("Destinations of the new data")
    private List<OutputConfig> outputs;
    private Job job;

    public SimpleJobConfig() {}

    public SimpleJobConfig(final String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<TriggerConfig> getTriggers() {
        return triggers;
    }

    @Override
    public InputConfig getInput() {
        return input;
    }

    @Override
    public MappingConfiguration getMappings() {
        return mappings;
    }

    @Override
    public List<OutputConfig> getOutputs() {
        return outputs;
    }

    public void setOutput(final List<OutputConfig> outputs) {
        this.outputs = outputs;
    }

    public void setMapping(final MappingConfiguration mappings) {
        Optional.ofNullable(mappings)
                .ifPresent(a -> a.setJobConfig(this));
        this.mappings = mappings;
    }

    public void setInput(final InputConfig input) {
        this.input = input;
    }

    public void setTrigger(final List<TriggerConfig> triggers) {
        this.triggers = triggers;
    }

    public void setName(final String name) {
        this.name = name;
    }
}