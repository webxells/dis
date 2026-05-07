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
package com.webxells.dis.plain.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;

@Description("Provides input data as plain string")
public class ToStringConfiguration implements InputConfig {
    @Description("Reference for this handler")
    private String name;
    @Required
    @Description("Where to receive the data from")
    private Resource receiver;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return ToString.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }
}
