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
package com.webxells.dis.rest.output;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.rest.PassConfiguration;

public class PassConfigurationToRest extends PassConfiguration implements Output<PassConfigurationToRestConfig> {
    private final Output<?> child;

    public PassConfigurationToRest(final PassConfigurationToRestConfig config) {
        final OutputConfig childConfig = config.getChild();
        findAll(ServiceManager.extractResourceOfConfig(childConfig));
        child = ServiceManager.loadByConfig(childConfig);
    }

    @Override
    public void write(final MappingConfiguration to) throws InputOutputError {
        setCurrent(to);
        child.write(to);
    }

    @Override
    public String getName() {
        return child.getName();
    }

    @Override
    public void start() throws DisException {
        child.start();
    }

    @Override
    public void end() throws DisException {
        child.end();
    }
}