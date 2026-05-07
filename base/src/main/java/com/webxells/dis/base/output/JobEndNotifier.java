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
package com.webxells.dis.base.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.base.trigger.OnJobEnd;

public class JobEndNotifier implements Output<JobEndNotifierConfig> {
    private String name;

    public JobEndNotifier(final JobEndNotifierConfig config) {
        name = config.getName();
    }

    @Override
    public void write(final MappingConfiguration to) { }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() throws DisException {  }

    @Override
    public void end() throws DisException {
        OnJobEnd.trigger(name);
    }
}
