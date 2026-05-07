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
package com.webxells.dis.time;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.time.intern.SimpleTimeApi;
import java.util.Optional;

@Description("Provides a fixed timestamp for further computation")
public class StaticTimePortrayal extends SimpleTimeApi implements TimePortrayal {
    @Required
    private String staticValue;

    public StaticTimePortrayal() {

    }

    public StaticTimePortrayal(final String staticValue) {
        this.staticValue = staticValue;
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == staticValue) {
            throw new InvalidApi("static value required");
        }
    }
    @Override
    public Optional<String> getValue(final MappingPart currentPart) {
        return Optional.of(staticValue);
    }

    public void setStaticValue(final String staticValue) {
        this.staticValue = staticValue;
    }
}
