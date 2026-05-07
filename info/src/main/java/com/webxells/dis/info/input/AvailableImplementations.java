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
package com.webxells.dis.info.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.config.Implementations;
import com.webxells.dis.info.internal.config.KnownTypeMapping;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AvailableImplementations implements Input<AvailableImplementationsConfig> {
    private final String name;
    private final boolean createMissingMappingParts;
    private final boolean knownMappingAsMap;
    private final List<String> interfaces;

    private boolean hasNext;


    public AvailableImplementations(final AvailableImplementationsConfig configuration) {
        name = configuration.getName();
        createMissingMappingParts = configuration.isCreateMissingMappingParts();
        knownMappingAsMap = configuration.isKnownMappingAsMap();
        interfaces = configuration.getInterfaces();
    }

    public int read(final MappingConfiguration from) throws InputOutputError {
        hasNext = false;
        final AtomicInteger result = new AtomicInteger(0);
        final DisImplementationManager disImplementationManager = new DisImplementationManager(interfaces);
        for (final MappingPart part : from.partsBySource(name)) {
            result.addAndGet(switch (part.getInput().getPath()) {
                case "implementations" ->  new Implementations(part, createMissingMappingParts)
                        .transform(disImplementationManager);
                case "knownTypeMapping" ->  new KnownTypeMapping(part, createMissingMappingParts, knownMappingAsMap)
                        .transform();
                default -> 0;
            });
        }
        return result.get();
    }

    @Override
    public boolean hasNext() {
        return hasNext;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void start() {
        hasNext = true;
    }

    @Override
    public void end() { }

}