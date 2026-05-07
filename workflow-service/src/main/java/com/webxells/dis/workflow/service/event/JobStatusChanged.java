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
package com.webxells.dis.workflow.service.event;

import com.webxells.dis.workflow.service.ServiceJob;
import com.webxells.dis.api.workflow.Job.JobState;

public class JobStatusChanged extends JobEvent {
    public final JobState oldState;
    public final JobState newState;

    public JobStatusChanged(final ServiceJob serviceJob, final JobState oldState, final JobState newState) {
        super(serviceJob);
        this.oldState = oldState;
        this.newState = newState;
    }

    public JobState getOldState() {
        return oldState;
    }

    public JobState getNewState() {
        return newState;
    }

    @Override
    public String toString() {
        return String.format("%s {from %s to %s}", JobStatusChanged.class.getName(), oldState, newState);
    }
}