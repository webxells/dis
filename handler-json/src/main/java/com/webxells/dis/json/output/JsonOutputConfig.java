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
package com.webxells.dis.json.output;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.config.description.RespectsRefinements;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.json.JsonConfig;
import com.webxells.dis.json.JsonType;
import com.webxells.dis.json.refinement.Nullable;
import java.util.Map;

@Description("Json writer")
@RespectsRefinements({Nullable.class})
public class JsonOutputConfig extends JsonConfig implements OutputConfig {
    public enum NoValueStrategy {
        @Description("Writes null")
        WRITE_NULL,
        @Description("Won't write value")
        IGNORE,
        @Description("Raises error")
        ERROR
    }

    public enum MultipleValuesForScalarFieldStrategy {
        @Description("Picks first value")
        FIRST,
        @Description("Picks last value")
        LAST,
        @Description("Raises error")
        ERROR
    }

    @Description("Static path, value mapping")
    private Map<String, String> staticMapping;
    @Required
    @Description("Defines where to send the json data to")
    private Resource sender;
    @Description("Forces types for given paths")
    private Map<String, JsonType> typeMapping = Map.of();
    @Description("How handling unexpected multi values")
    @Default("FIRST")
    private MultipleValuesForScalarFieldStrategy multipleValuesForScalarFieldStrategy =
            MultipleValuesForScalarFieldStrategy.FIRST;
    @Description("Respect references for subData")
    @Default("false")
    private boolean subDataToReferenceRestricted;
    @Description("Handles case that no value for a path is present")
    @Default("IGNORE")
    private NoValueStrategy noValueStrategy = NoValueStrategy.IGNORE;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
    }

    @Override
    public String getType() {
        return JsonOutput.class.getName();
    }

    public NoValueStrategy getNoValueStrategy() {
        return noValueStrategy;
    }

    public void setNoValueStrategy(final NoValueStrategy noValueStrategy) {
        this.noValueStrategy = noValueStrategy;
    }

    public boolean isSubDataToReferenceRestricted() {
        return subDataToReferenceRestricted;
    }

    public void setSubDataToReferenceRestricted(final boolean subDataToReferenceRestricted) {
        this.subDataToReferenceRestricted = subDataToReferenceRestricted;
    }

    public Resource getSender() {
        return sender;
    }

    public void setSender(final Resource sender) {
        this.sender = sender;
    }

    public void setTypeMapping(final Map<String, JsonType> typeMapping) {
        this.typeMapping = typeMapping;
    }

    public Map<String, JsonType> getTypeMapping() {
        return typeMapping;
    }

    public Map<String, String> getStaticMapping() {
        return staticMapping;
    }

    public void setStaticMapping(final Map<String, String> staticMapping) {
        this.staticMapping = staticMapping;
    }

    public MultipleValuesForScalarFieldStrategy getMultipleValuesForScalarFieldStrategy() {
        return multipleValuesForScalarFieldStrategy;
    }

    public void setMultipleValuesForScalarFieldStrategy(final MultipleValuesForScalarFieldStrategy multipleValuesForScalarFieldStrategy) {
        this.multipleValuesForScalarFieldStrategy = multipleValuesForScalarFieldStrategy;
    }
}