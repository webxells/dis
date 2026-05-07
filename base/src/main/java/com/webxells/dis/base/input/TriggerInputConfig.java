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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

@Description("Input to fetch MappingParts read by InputTrigger. " +
        "FYI: using InputTrigger.triggerStrategy = HasAny, no data won't be able to be fetched")
public class TriggerInputConfig implements InputConfig {
    @Description("Same of targeted InputTrigger. " +
            "Threadsafe: won't be able to fetch data of other jobs/triggers")
    @Required
    private String name;

    @Override
    public String getType() {
        return TriggerInput.class.getName();
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}