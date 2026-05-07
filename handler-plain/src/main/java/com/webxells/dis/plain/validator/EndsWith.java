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
package com.webxells.dis.plain.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.validator.SimpleValidator;
import java.util.Optional;

@Description("Checks if value ends with a specific postfix")
public class EndsWith extends SimpleValidator {
    @Required
    @Description("Postfix to look for")
    private String value;

    @Override
    public void validate() throws InvalidApi {
        if (null == value) {
            throw new InvalidApi("value required");
        }
    }

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return Optional.ofNullable(currentPiece)
                .flatMap(DatasetPiece::value)
                .filter(a -> a.endsWith(value))
                .isPresent();
    }

    public void setValue(final String value) {
        this.value = value;
    }
}