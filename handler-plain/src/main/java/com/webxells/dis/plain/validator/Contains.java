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
import com.webxells.dis.base.validator.IsSet;
import java.util.List;
import java.util.Optional;

@Description("Checks if given value is contained")
public class Contains extends IsSet {
    @Required(xor = "values")
    private String value;
    @Description("multiple values combined by 'or'")
    @Required(xor = "value")
    private List<String> values;

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) &&
                currentPiece.value()
                        .map(this::containsIn)
                        .orElse(false);
    }

    private boolean containsIn(final String currentValue) {
        return Optional.ofNullable(value)
                .map(currentValue::contains)
                .orElseGet(() -> Optional.of(values)
                        .map(a -> values.stream()
                                .anyMatch(currentValue::contains))
                    .orElse(false)
                );
    }

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == value && null == values) {
            throw new InvalidApi("value or values is required");
        }
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }
}