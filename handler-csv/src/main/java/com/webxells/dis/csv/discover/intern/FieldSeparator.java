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

import java.util.List;

import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.MINE;
import static com.webxells.dis.csv.discover.intern.CsvPattern.OWNING_RESULT.NOPE;

public class FieldSeparator implements CsvElement {
    public static final List<Character> LIKELY_CHARS = List.of(';', ',', '#', '~', '\t', '|');

    private Character separator;
    @Override
    public void resetRow() {
        separator = null;
    }

    @Override
    public CsvPattern.OWNING_RESULT match(final String rawLne, final int pos) {
        final char current = rawLne.charAt(pos);
        if (null == separator) {
            if (isSeparatorChar(current)) {
                separator = current;
                return MINE;
            }
        }
        return null != separator && separator == current ? MINE : NOPE;
    }

    private boolean isSeparatorChar(final char current) {
        return LIKELY_CHARS.contains(current);
    }

    public Character getChar() {
        return separator;
    }
}