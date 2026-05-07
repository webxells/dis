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

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.output.Output;

public class OutputWritten extends FilledDatasetEvent {
    private final Output<?> output;

    public OutputWritten(final long position, final MappingConfiguration mappingConfiguration, final Output<?> output) {
        super(position, mappingConfiguration);
        this.output = output;
    }

    public Output<?> getOutput() {
        return output;
    }
}