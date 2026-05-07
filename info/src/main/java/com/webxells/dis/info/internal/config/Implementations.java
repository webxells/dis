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
package com.webxells.dis.info.internal.config;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.config.transformer.InterfaceName;
import com.webxells.dis.info.internal.config.transformer.Modules;
import java.util.concurrent.atomic.AtomicInteger;

public class Implementations extends RootConfiguration {

    public Implementations(final MappingPart part, final boolean createMissingMappingParts) {
        super(part, createMissingMappingParts);
    }

    public int transform(final DisImplementationManager disImplementationManager) throws InputOutputError {
        final AtomicInteger count = new AtomicInteger();
        final Configuration config = new Configuration(disImplementationManager);
        disImplementationManager.forEachInterface(a -> {
            final MappingConfiguration current = count.get() > 0 ? part.createNewSubData() : part.getFirstOrNewSubData();
            count.addAndGet(new InterfaceName(a, config).transform(current));
            count.addAndGet(new Modules(config).transform(a, current));
        });
        return count.get();
    }

}