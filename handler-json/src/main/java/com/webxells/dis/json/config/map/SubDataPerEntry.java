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
package com.webxells.dis.json.config.map;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.SimpleDatasetPiece;

@Description("MappingPart that represents a Json map")
public class SubDataPerEntry extends MapEatingPart {
    @Description("Mapping part of value")
    @Required
    protected MappingPortrayal valuePortrayal;
    @Description("Mapping part of value")
    @Required
    protected MappingPortrayal titlePortrayal;
    public SubDataPerEntry(final MappingConfiguration configuration) {
        super(configuration);
    }

    public SubDataPerEntry(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == titlePortrayal || null == valuePortrayal) {
            throw new InvalidApi("title and value portrayal required");
        }
    }

    @Override
    protected void saveEntry(final String title, final MappingPart mappingPart) {
        final MappingConfiguration configuration = getRegardingSubData();
        configuration.getByPortrayal(titlePortrayal)
                .ifPresent(a -> a.getDataset().collect(new SimpleDatasetPiece(title)));
        configuration.getByPortrayal(valuePortrayal)
                //too easy thinking? - need 2 find parts for subData and copyValues
                .ifPresent(a -> a.copyValues(mappingPart));
        size++;
    }

    private MappingConfiguration getRegardingSubData() {
        final MappingConfiguration configuration = getSubData().get(0);
        if (0 == size) {
            return configuration;
        }
        final MappingConfiguration copy = configuration.copy();
        copy.clear();
        getSubData().add(copy);
        return copy;
    }

    public void setPortrayalForTitle(final MappingPortrayal titlePortrayal) {
        this.titlePortrayal = titlePortrayal;
    }

    public void setPortrayalForValue(final MappingPortrayal valuePortrayal) {
        this.valuePortrayal = valuePortrayal;
    }
}