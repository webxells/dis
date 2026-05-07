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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Description("Checks value for its type")
public class TypeAssertions extends NotNull {
    public enum Type {
        @Description("Positive numbers (not decimals)")
        POSITIVE_NUMERIC(List.of()),
        @Description("false or true")
        BOOLEAN(List.of()),
        @Description("All numbers (not decimals)")
        NUMERIC(List.of(POSITIVE_NUMERIC)),
        @Description("Positive decimals (includes positive numbers)")
        POSITIVE_DECIMAL(List.of(POSITIVE_NUMERIC)),
        @Description("All decimals or numbers")
        DECIMAL(List.of(POSITIVE_DECIMAL, POSITIVE_NUMERIC, NUMERIC)),
        @Description("Text")
        STRING(List.of(POSITIVE_NUMERIC, POSITIVE_DECIMAL, NUMERIC, DECIMAL, BOOLEAN));

        private final List<Type> isOkayWithThat;

        Type(final List<Type> isOkayWithThat) {
            this.isOkayWithThat = isOkayWithThat;
        }

        private boolean isOkayWithThat(final Type type) {
            return this == type || isOkayWithThat.contains(type);
        }
    }

    @Required
    private Type assertionType;

    @Description("Treats whitespaces as text and does not skip it")
    @Default("false")
    private boolean respectWhitespaces;

    @Override
    public boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return super.validateCurrentPiece(currentPiece, mappingPart) &&
                assertType(currentPiece.value().get());
    }

    public void setAssertionType(final Type assertionType) {
        this.assertionType = assertionType;
    }

    public void setRespectWhitespaces(final boolean respectWhitespaces) {
        this.respectWhitespaces = respectWhitespaces;
    }

    private boolean assertType(final String value) {
        return assertionType.isOkayWithThat(parseType(value));
    }

    private Type parseType(final String value) {
        if (value == null || value.isEmpty()) {
            return Type.STRING;
        }
        if (isBooleanValue(value)) {
            return Type.BOOLEAN;
        }
        boolean positive = true;
        boolean decimal = false;
        int whiteSpaceStart = 0;
        final byte[] data = value.getBytes(StandardCharsets.UTF_8);
        for (int i = 0, m = data.length; i < m; i++) {
            final char current = (char) data[i];
            if (0 == i - whiteSpaceStart) {
                if (!respectWhitespaces && Character.isWhitespace(current)) {
                    whiteSpaceStart++;
                    continue;
                } else if (current == '-' || current == '+') {
                    positive = current == '+';
                    continue;
                }
            }
            if (current == '.' || current == ',') {
                if (decimal || i + 1 == m || i - whiteSpaceStart == 0) {
                    return Type.STRING;
                }
                decimal = true;
                continue;
            }
            if (47 < current && 58 > current) {
                continue;
            }
            if (!respectWhitespaces && Character.isWhitespace(current)) {
                continue;
            }
            return Type.STRING;
        }
        return getNumericType(positive, decimal);
    }

    private boolean isBooleanValue(final String value) {
        return "false".equalsIgnoreCase(value) || "true".equalsIgnoreCase(value);
    }

    private Type getNumericType(final boolean positive, final boolean decimal) {
        if (positive) {
            if (decimal) {
                return Type.POSITIVE_DECIMAL;
            }
            return Type.POSITIVE_NUMERIC;
        }
        if (decimal) {
            return Type.DECIMAL;
        }
        return Type.NUMERIC;
    }
}