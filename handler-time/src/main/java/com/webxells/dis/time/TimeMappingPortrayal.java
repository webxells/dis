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

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.time.intern.SimpleTimeApi;
import java.util.Optional;

@Description("Sets time meta data, like format and zone")
public class TimeMappingPortrayal extends SimpleTimeApi implements TimePortrayal {
    @Required
    @Description("Mapping part with time data")
    private MappingPortrayal mappingPortrayal;

    @Override
    public void validate() throws InvalidApi {
        if (null == mappingPortrayal) {
            throw new InvalidApi("mapping portrayal required");
        }
    }

    public void setMappingPortrayal(final MappingPortrayal mappingPortrayal) {
        this.mappingPortrayal = mappingPortrayal;
    }

    @Override
    public Optional<String> getValue(final MappingPart currentPart) {
        return Optional.ofNullable(currentPart)
                .map(MappingPart::getConfiguration)
                .flatMap(a -> a.getByPortrayal(mappingPortrayal))
                .flatMap(MappingPart::value);
    }



}
