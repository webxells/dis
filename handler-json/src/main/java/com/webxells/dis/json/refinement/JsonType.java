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
package com.webxells.dis.json.refinement;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.refinement.SimpleRefinement;

@Description("Defines array type")
public class JsonType extends SimpleRefinement {
    public enum Type {
        @Description ("Converts array entries to multiple DataSetPieces") ARRAY_TO_MULTIPLE_DATASET_PIECES,
        @Description ("Default behaviour like without Refinement") DEFAULT
    };

    @Required
    private Type jsonType;

    @Override
    public void validate() throws InvalidApi {
        if (null == jsonType) {
            throw new InvalidApi("jsonType is required");
        }
    }

    @Description("Sets specific type")
    public void setJsonType(final Type jsonType) {
        this.jsonType = jsonType;
    }

    public Type getJsonType() {
        return jsonType;
    }
}