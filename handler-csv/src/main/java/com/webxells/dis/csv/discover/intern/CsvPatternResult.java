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
package com.webxells.dis.csv.discover.intern;

import java.util.Objects;

public class CsvPatternResult {
    private final Character fieldSeparator;
    private final Character valueSeparator;

    public CsvPatternResult(final Character fieldSeparator, final Character valueSeparator) {
        this.fieldSeparator = fieldSeparator;
        this.valueSeparator = valueSeparator;
    }

    public Character getFieldSeparator() {
        return fieldSeparator;
    }

    public Character getValueSeparator() {
        return valueSeparator;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final CsvPatternResult that = (CsvPatternResult) o;
        return Objects.equals(fieldSeparator, that.fieldSeparator) && Objects.equals(valueSeparator, that.valueSeparator);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldSeparator, valueSeparator);
    }
}