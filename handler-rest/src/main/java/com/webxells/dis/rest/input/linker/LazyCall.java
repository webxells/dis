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
package com.webxells.dis.rest.input.linker;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.base.input.linker.SimpleInputLinker;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.rest.Rest;

import java.util.Optional;

@Description("Uses an input with a rest call as a resource to read data")
public class LazyCall extends SimpleInputLinker {
    private Rest restResource;
    private boolean lookedUpMethod;
    @Description("Looks for nested resources")
    @Default("false")
    private boolean lookForNestedRestResource;

    @Override
    public void validate() throws InvalidApi {
        if (noReceiver()) {
            throw new InvalidApi("config with receiver required");
        }
    }

    @Override
    public int getData(final MappingConfiguration from) throws InputOutputError {
        setRootDataset(from);
        try {
            final Input<? extends InputConfig> input = getInput();
            input.start();
            final int read = input.read(from);
            input.end();
            return read;
        } catch (DisException e) {
            throw new InputOutputError("Something failed in embedded input", e);
        }
    }

    private boolean noReceiver() {
        return null == getReceiverRestResource();
    }

    private Rest getReceiverRestResource() {
        if (!lookedUpMethod) {
            final Resource resource = ServiceManager.extractResourceOfConfig(inputConfig);
            restResource = lookForRestResource(resource);
            lookedUpMethod = true;
        }
        return restResource;
    }

    private Rest lookForRestResource(Resource resource) {
        do {
            if (resource instanceof Rest) {
                return (Rest) resource;
            }
        } while(lookForNestedRestResource &&
                null != (resource = ServiceManager.extractReceiverOfConfig(resource)));
        return null;
    }

    private void setRootDataset(final MappingConfiguration rootData) {
        final Rest resource = getReceiverRestResource();
        Optional.ofNullable(resource.getRequestContentStrategy())
                .ifPresent(a -> a.forEach(c -> c.setContent(rootData)));
    }

    @Override
    public void start() { }

    @Override
    public void end() { }

    public void setLookForNestedRestResource(final boolean lookForNestedRestResource) {
        this.lookForNestedRestResource = lookForNestedRestResource;
    }
}