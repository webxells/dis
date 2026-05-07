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
package com.webxells.dis.plain.intern;

import com.webxells.dis.api.MappingOperation;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.plain.Option;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

public abstract class RegexOperation implements MappingOperation {
    @Description("Regular expression to use")
    @Required(xor = {"searchByMappingPortrayal"})
    protected String search;
    @Required(xor = {"search"})
    @Description("MappingPart that contains the regular expression")
    protected MappingPortrayal searchByMappingPortrayal;
    @Description("Search options")
    protected Set<Option> options = Set.of();

    @Override
    public void validate() throws InvalidApi {
        if (null == search && null == searchByMappingPortrayal) {
            throw new InvalidApi("search or searchByMappingPortrayal required");
        }
    }

    @SuppressWarnings("MagicConstant")
    protected Pattern getRegex(final MappingConfiguration configuration) {
        return  Pattern.compile(Objects.requireNonNullElseGet(search, () -> configuration.getByPortrayal(searchByMappingPortrayal)
                            .flatMap(MappingPart::value).orElse("")),
                    Option.toFlagValue(Set.copyOf(options)));
    }

    public void setSearch(final String search) {
        this.search = search;
    }

    public void setSearchByMappingPortrayal(final MappingPortrayal searchByMappingPortrayal) {
        this.searchByMappingPortrayal = searchByMappingPortrayal;
    }

    //@todo: gson bug => List<Option> works - Set<Option> doesn't
    public void setOptions(final List<Option> options) {
        this.options = new HashSet<>(options);
    }
}