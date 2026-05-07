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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.validator.IsSet;
import java.util.function.BiFunction;

@Description("Compares value length with given number")
public class Length extends IsSet {
    public enum Operator {
        @Description("a < b") LESS((a, b) -> a < b),
        @Description("a <= b") LESS_OR_EQUAL((a, b) -> a <= b),
        @Description("a = b") EQUAL((a, b) -> a == b),
        @Description("a <> b") NOT_EQUAL((a, b) -> a != b),
        @Description("a > b") MORE((a, b) -> a > b),
        @Description("a >= b") MORE_OR_EQUAL((a, b) -> a >= b);
        private final BiFunction<Integer, Integer, Boolean> operatingFunction;

        Operator(final BiFunction<Integer, Integer, Boolean> operatingFunction) {
            this.operatingFunction = operatingFunction;
        }

        private boolean apply(final int length, final int comparative) {
            return operatingFunction.apply(length, comparative);
        }
    }

    @Description("Compares with this value")
    @Required
    private Integer comparativeValue;
    @Required
    @Description("Defines how to compare both values")
    private Operator operator;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == operator || null == comparativeValue) {
            throw new InvalidApi("required fields are missing");
        }
    }

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) &&
                operator.apply(currentPiece.value().get().length(), comparativeValue);
    }

    public void setComparativeValue(final int comparativeValue) {
        this.comparativeValue = comparativeValue;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }
}