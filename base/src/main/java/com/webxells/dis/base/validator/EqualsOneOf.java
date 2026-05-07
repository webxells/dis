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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.util.List;

@Description("Validates that the value equals one of the given values")
public class EqualsOneOf extends IsSet {
    @Required
    private List<String> values;

    @Override
    public void validate() throws InvalidApi {
        if (null == values) {
            throw new InvalidApi("field values required!");
        }
    }

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) && values.contains(currentPiece.value().get());
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }
}