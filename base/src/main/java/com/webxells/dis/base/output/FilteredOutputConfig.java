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
package com.webxells.dis.base.output;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.validator.Validator;
import java.util.List;

@Description("Writes data into provided Output only if validation of provided Validator was successful")
public class FilteredOutputConfig implements OutputConfig {
    public static class FilterEntry {
        public Validator validator;
        public OutputConfig output;
        @Description("Portrayal to find mapping part used by Validator")
        public MappingPortrayal portrayal;
    }

    @Description("List of filter entries")
    private List<FilterEntry> filterEntries;
    @Description("Reference of this handler")
    @Required
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return FilteredOutput.class.getName();
    }

    public List<FilterEntry> getFilterEntries() {
        return filterEntries;
    }

    public void setFilterEntries(final List<FilterEntry> filterEntries) {
        this.filterEntries = filterEntries;
    }

    public void setName(final String name) {
        this.name = name;
    }
}