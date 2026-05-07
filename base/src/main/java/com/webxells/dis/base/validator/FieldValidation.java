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
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.base.SimpleDatasetPiece;

@Description("Running provided validator with portrayed MappingPart")
public class FieldValidation extends SimpleValidator {
    @Required
    private MappingPortrayal field;
    @Required
    private Validator validate;

    @Override
    public void validate() throws InvalidApi {
        if (null == field || null == validate) {
            throw new InvalidApi("Required fields missing");
        }
    }

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return mappingPart.getConfiguration().getByPortrayal(field)
                .map(a -> validate.validate(new SimpleDatasetPiece(a.value().orElse(null)), a))
                .orElse(false);
    }


    public void setField(final MappingPortrayal field) {
        this.field = field;
    }

    public void setValidate(final Validator validate) {
        this.validate = validate;
    }
}