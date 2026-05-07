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
package com.webxells.dis.workflow.service.meta.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.workflow.Job;
import com.webxells.dis.base.output.FilteredOutput;
import com.webxells.dis.base.output.FilteredOutputConfig.FilterEntry;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.event.EventManager;
import com.webxells.dis.workflow.service.event.ChildOutputWritten;
import com.webxells.dis.workflow.service.meta.output.CountingFilteredOutputConfig.CountingFilterEntry;

public class CountingFilteredOutput extends FilteredOutput implements G<CountingFilteredOutputConfig> {
    private EventManager.ContextProxy eventManager;

    public CountingFilteredOutput(final CountingFilteredOutputConfig configuration) {
        super(configuration);
    }

    @Override
    protected boolean filterMatched(final FilterEntry current, final MappingConfiguration to) {
        final boolean result = super.filterMatched(current, to);
        if (result && current instanceof CountingFilterEntry countingFilterEntry) {
            try {
                getEventManager(WorkflowSecurity.instance().getJob(to.getJobName()))
                        .trigger(new ChildOutputWritten(countingFilterEntry.aliasName, getOutput(current)));
            } catch (final DisException ignored) { }
        }
        return result;
    }

    private EventManager.ContextProxy getEventManager(final Job job) {
        if (null == eventManager) {
            eventManager = EventManager.instance().new ContextProxy(job);
        }
        return eventManager;
    }
}