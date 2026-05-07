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
import java.util.Objects;
import java.util.function.BiFunction;

@Description("Uses various Math operations for validation")
public class Math extends IsSet {
    public enum Operation {
        EQUALS(Objects::equals),
        NOT_EQUALS((a, b) -> !Objects.equals(a, b)),
        MOD_IS_ZERO((a, b) -> a % b == 0),
        MOD_IS_NOT_ZERO((a, b) -> a % b != 0),
        LOWER((a, b) -> a < b),
        LOWER_EQUALS((a, b) -> a <= b),
        GREATER_EQUALS((a, b) -> a >= b),
        GREATER((a, b) -> a > b);

        private final BiFunction<Double, Double, Boolean> function;

        Operation(final BiFunction<Double, Double, Boolean> function) {
            this.function = function;
        }

        boolean apply(final double value1, final double value2) {
            return function.apply(value1, value2);
        }
    }

    @Required
    private Operation operation;
    @Required(xor = {"comparativePortrayal"})
    @Description("Value to compare with")
    private String comparativeValue;
    @Required(xor = {"comparativeValue"})
    @Description("MappingPart that holds the value to compare with")
    private MappingPortrayal comparativePortrayal;

    @Override
    public void validate() throws InvalidApi {
        if (null == operation || (null == comparativePortrayal && null == comparativeValue)) {
            throw new InvalidApi("Required parameter missing");
        }
    }

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) &&
                checkMathComparison(currentPiece.value().get(), getComparativeValue(mappingPart));
    }

    public void setOperation(final Operation operation) {
        this.operation = operation;
    }

    public void setComparativeValue(final String comparativeValue) {
        this.comparativeValue = comparativeValue;
    }

    public void setComparativePortrayal(final MappingPortrayal comparativePortrayal) {
        this.comparativePortrayal = comparativePortrayal;
    }

    private boolean checkMathComparison(final String value, final String fetchedComparativeValue) {
        try {
            return operation.apply(toDouble(value), toDouble(fetchedComparativeValue));
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    private String getComparativeValue(final MappingPart mappingPart) {
        if (null == comparativePortrayal || null == mappingPart) {
            return comparativeValue;
        }
        return mappingPart.getConfiguration()
                .getByPortrayal(comparativePortrayal)
                   .flatMap(MappingPart::value)
                   .orElse("");
    }

    private double toDouble(final String value) {
        return Double.parseDouble(value.replaceAll("\\s+", "")
                .replace(',', '.'));
    }


}