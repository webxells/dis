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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.boot.ServiceManager;
import java.util.Optional;

@Description("Makes current mapping parts usable in given input")
public class PassConfigurationToRestConfig implements InputConfig {
    @Required
    private InputConfig child;

    public InputConfig getChild() {
        return child;
    }

    public void setChild(final InputConfig child) {
        this.child = child;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return PassConfigurationToRest.class.getName();
    }

    public Resource getReceiver() {
        return Optional.ofNullable(child)
                .map(a -> ServiceManager.extractResourceOfConfig(child))
                .orElse(null);
    }
}