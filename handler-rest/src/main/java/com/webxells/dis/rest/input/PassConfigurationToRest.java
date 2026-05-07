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
package com.webxells.dis.rest.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.ParentInputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.rest.PassConfiguration;

public class PassConfigurationToRest extends PassConfiguration implements Input<PassConfigurationToRestConfig> {
    private final Input<?> child;
    private boolean started;

    public PassConfigurationToRest(final PassConfigurationToRestConfig config) {
        final InputConfig childConfig = config.getChild();
        findAll(ServiceManager.extractResourceOfConfig(childConfig));
        if (childConfig instanceof ParentInputConfig parentInput) {
            parentInput.getChildren().forEach(a -> findAll(ServiceManager.extractResourceOfConfig(a)));
        }
        child = ServiceManager.loadByConfig(childConfig);
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        setCurrent(from);
        if (!started) {
            startChild();
        }
        return child.read(from);
    }

    private void startChild() throws InputOutputError {
        started = true;
        try {
            child.start();
        } catch (final DisException e) {
            throw new InputOutputError("Child could not be started", e);
        }
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        if (!started && restResources.stream().anyMatch(a -> a instanceof MultiResource)) {
            startChild();
        }
        return child.hasNext();
    }

    @Override
    public String getName() {
        return child.getName();
    }

    @Override
    public void start() { }

    @Override
    public void end() throws DisException {
        child.end();
        started = false;
    }
}