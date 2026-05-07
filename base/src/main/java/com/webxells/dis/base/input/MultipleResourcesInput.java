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
package com.webxells.dis.base.input;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.resource.MultiResource;
import com.webxells.dis.api.resource.MultiResource.RefreshResult;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.boot.ServiceManager;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MultipleResourcesInput implements Input<MultipleResourcesInputConfig> {
    public static class StopRefreshing implements Validator  {
        @Override
        public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
            STOP_REGISTRY.add(Thread.currentThread());
            LOGGER.d("Next refreshing blocked");
            return true;
        }
    }


    private static final Logger LOGGER = LoggerProxyFactory.logger(MultipleResourcesInput.class);
    private static final Set<Thread> STOP_REGISTRY = Collections.synchronizedSet(new HashSet<>());

    private final InputConfig inputConfig;
    private final boolean skipChildError;

    private Input<? extends InputConfig> child;
    private boolean lookedUpMethod;
    private Resource resource;
    private boolean errorOccurred;

    public MultipleResourcesInput(final MultipleResourcesInputConfig config) {
        inputConfig = config.getInput();
        skipChildError = config.isChildErrorToStop();
        child = ServiceManager.loadByConfig(inputConfig);
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        final int readValuesCount = child.read(from);
        if (0 == readValuesCount) {
            if (RefreshResult.NONE != refreshResource()) {
                return child.read(from);
            }
        }
        return readValuesCount;
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        if (!child.hasNext()) {
            if (RefreshResult.NONE == refreshResource()) {
                return false;
            }
        }
        return child.hasNext();
    }

    @Override
    public String getName() {
        return child.getName();
    }

    @Override
    public void start() throws DisException {
        final Resource resource = getResource();
        if (resource instanceof MultiResource) {
            ((MultiResource) resource).reset();
        }
        child.start();
        clearRegistry();
    }

    private void clearRegistry() {
        STOP_REGISTRY.remove(Thread.currentThread());
    }

    @Override
    public void end() throws DisException {
        if (errorOccurred) {
            errorOccurred = false;
        } else {
            child.end();
        }
        clearRegistry();
    }

    private RefreshResult refreshResource() throws InputOutputError {
        try {
            return refreshInputResource();
        } catch (DisException e) {
            if (skipChildError) {
                LOGGER.d("skipping child error", e);
                errorOccurred = true;
                return RefreshResult.NONE;
            }
            throw new InputOutputError("Refreshing resource failed", e);
        }
    }

    private RefreshResult refreshInputResource() throws DisException {
        Resource resource = getResource();
        if (noStopRequestForThis() && resource instanceof MultiResource) {
            final RefreshResult result = ((MultiResource) resource).refresh();
            if (RefreshResult.NONE != result) {
                child.end();
                child = ServiceManager.loadByConfig(inputConfig);
                child.start();
            }
            return result;
        }
        return RefreshResult.NONE;
    }

    private boolean noStopRequestForThis() {
        final Thread current = Thread.currentThread();
        if (STOP_REGISTRY.contains(current)) {
            LOGGER.d("Got stop request for current refresh - will stop ");
            STOP_REGISTRY.remove(current);
            return false;
        }
        return true;
    }

    private Resource getResource() {
        if (!lookedUpMethod) {
            resource = ServiceManager.extractResourceOfConfig(inputConfig);
            lookedUpMethod = true;
        }
        return resource;
    }

}