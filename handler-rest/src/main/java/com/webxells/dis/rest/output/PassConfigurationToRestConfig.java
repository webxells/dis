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

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;

@Description("Makes content of current configuration usable in given child output")
public class PassConfigurationToRestConfig implements OutputConfig {
    @Required
    @Description("Output that handles writing")
    private OutputConfig child;
    @Description("If not given, it looks for sender within the child output")
    @Required
    private Resource sender;

    @Override
    public String getName() {
        return null;
    }

    @Override
    public String getType() {
        return PassConfigurationToRest.class.getName();
    }

    public OutputConfig getChild() {
        return child;
    }

    public void setChild(final OutputConfig child) {
        this.child = child;
    }

    public Resource getSender() {
        return sender;
    }

    public void setSender(final Resource sender) {
        this.sender = sender;
    }
}