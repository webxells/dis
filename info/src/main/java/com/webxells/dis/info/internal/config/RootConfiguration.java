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

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.PartWrapper;
import java.util.Optional;

public abstract class RootConfiguration {
    public class Configuration {
        private final DisImplementationManager disImplementationManager;

        public Configuration(final DisImplementationManager disImplementationManager) {
            this.disImplementationManager = disImplementationManager;
        }

        public String inputReference() {
            return inputReference;
        }

        public String outputReference() {
            return outputReference;
        }

        public boolean createMissingMappingParts() {
            return createMissingMappingParts;
        }

        public DisImplementationManager disImplementationManager() {
            return disImplementationManager;
        }
    }

    protected final String inputReference;
    protected final String outputReference;
    protected final PartWrapper part;
    protected final boolean createMissingMappingParts;

    public RootConfiguration(final MappingPart part, final boolean createMissingMappingParts) {
        this.part = new PartWrapper(part);
        this.createMissingMappingParts = createMissingMappingParts;
        inputReference = part.getInput().getReference();
        outputReference = Optional.ofNullable(part.getOutput())
                .map(MappingPoint::getReference)
                .orElse(inputReference);
    }
}