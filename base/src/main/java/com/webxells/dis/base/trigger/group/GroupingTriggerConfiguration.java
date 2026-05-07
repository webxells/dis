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
package com.webxells.dis.base.trigger.group;

import com.webxells.dis.api.RuntimeEnvironment;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.trigger.SimpleConcurrentTriggerConfig;
import java.util.List;

@Description("Grouping several Dis jobs by field group. Configuration is only required once.")
public class GroupingTriggerConfiguration extends SimpleConcurrentTriggerConfig {
    public enum UpdateEvent {
        START, END, ERROR
    }
    public enum ErrorStrategy {
        @Description("Run with error will be restarted") RESTART_CHILD,
        @Description("All runs will be aborted and restarted from first child") RESTART_ALL,
        @Description("Stop all runs and restart to wait for start to be triggered") RESTART_LISTENING,
        @Description("Stop whole Dis job by error") STOP,
        @Description("Ignore error and continue with other children") CONTINUE
    }

    @Description("Trigger to start group, there is only one GroupingTrigger with provided start")
    private TriggerConfig start;
    @Description("How many triggers may triggered parallel; n >= 1")
    @Default("1 -> one by one")
    private int maxParallel;
    @Description("Group name")
    @Required
    private String group;
    @Default("Raw Threads")
    private RuntimeEnvironment runtimeEnvironment;
    @Description("""
            Output for sending status updates. Available paths:
            name: name of group
            start: start date
            end: end date
            status: status (INITIALIZED, STARTED, ERROR, FINISHED, FINISHED (ERROR))
            children: subData of child triggers with paths of
                name: name of child
                status: status (INITIALIZED, RUNNING, ERROR, FINISHED, ABORTED)
                start: start date
                end: end date
                error: if any occurred
                error-trace: if any occurred""")
    @Default("Don't send any updates")
    private OutputConfig statusUpdateOutput;
    @Default("dd.MM.yyyy HH:mm:ss")
    private String statusUpdateDateFormat;
    @Description("When to update")
    @Default("[END]")
    private List<UpdateEvent> eventsToUpdate;
    @Description("Strategy how to handle errors occurring in child runs")
    @Default("STOP")
    private ErrorStrategy errorStrategy;

    @Override
    public void validate() throws InvalidApi {
        if (null == group) {
            throw new InvalidApi("group is required");
        }
    }

    @Override
    public String getType() {
        return GroupingTrigger.class.getName();
    }

    public TriggerConfig getStart() {
        return start;
    }

    public void setStart(final TriggerConfig start) {
        this.start = start;
    }

    public int getMaxParallel() {
        return maxParallel;
    }

    public void setMaxParallel(final int maxParallel) {
        this.maxParallel = maxParallel;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(final String group) {
        this.group = group;
    }

    @Override
    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    @Override
    public void setRuntimeEnvironment(final RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public List<UpdateEvent> getEventsToUpdate() {
        return eventsToUpdate;
    }

    public void setEventsToUpdate(final List<UpdateEvent> eventsToUpdate) {
        this.eventsToUpdate = eventsToUpdate;
    }

    public OutputConfig getStatusUpdateOutput() {
        return statusUpdateOutput;
    }

    public void setStatusUpdateOutput(final OutputConfig statusUpdateOutput) {
        this.statusUpdateOutput = statusUpdateOutput;
    }

    public ErrorStrategy getErrorStrategy() {
        return errorStrategy;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public String getStatusUpdateDateFormat() {
        return statusUpdateDateFormat;
    }

    public void setStatusUpdateDateFormat(final String statusUpdateDateFormat) {
        this.statusUpdateDateFormat = statusUpdateDateFormat;
    }
}