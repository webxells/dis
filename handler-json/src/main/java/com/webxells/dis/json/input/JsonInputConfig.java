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
package com.webxells.dis.json.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.config.description.RespectsRefinements;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import com.webxells.dis.json.JsonConfig;
import com.webxells.dis.json.refinement.JsonType;
import java.util.Map;

@Description("Json reader")
@RespectsRefinements({JsonType.class})
public class JsonInputConfig extends JsonConfig implements InputConfig {
    public enum UnreachablePathStrategy {
        @Description("Raises error") FAIL, @Description("Defaults to null") NULL, @Description("Defaults to an empty string") EMPTY,
        @Description("Ignores path") IGNORE;
    }
    public enum ReadType {
        @Description("Raw json") RAW, @Description("parsed value") JSON_VALUE
    }

    @Required
    @Description("Defines where to receive the json data from")
    private Resource receiver;
    @Description("Handles case that a path could not be read")
    @Default("EMPTY")
    private UnreachablePathStrategy unreachablePathStrategy = UnreachablePathStrategy.EMPTY;
    @Description("Json paths with RAW readType are read as raw json")
    private Map<String, ReadType> readTypes;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == receiver) {
            throw new InvalidApi("retriever required");
        }
    }

    @Override
    public String getType() {
        return JsonInput.class.getName();
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public UnreachablePathStrategy getUnreachablePathStrategy() {
        return unreachablePathStrategy;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setUnreachablePathStrategy(final UnreachablePathStrategy unreachablePathStrategy) {
        this.unreachablePathStrategy = unreachablePathStrategy;
    }

    public Map<String, ReadType> getReadTypes() {
        return readTypes;
    }

    public void setReadTypes(final Map<String, ReadType> readTypes) {
        this.readTypes = readTypes;
    }
}