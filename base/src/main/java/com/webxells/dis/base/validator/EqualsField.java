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
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;

@Description("Validates that the value equals value from another MappingPart")
public class EqualsField extends IsSet {
    @Required
    private MappingPortrayal field;

    @Override
    public void validate() throws InvalidApi {
        if (null == field) {
            throw new InvalidApi("field is missing");
        }
    }

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) &&
                valueIsEqualWithField(currentPiece, mappingPart);
    }

    private boolean valueIsEqualWithField(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return currentPiece.value()
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(field)
                                .flatMap(MappingPart::value)
                                .map(b -> b.equals(a)))
                .orElse(false);
    }

    public void setField(final MappingPortrayal field) {
        this.field = field;
    }
}