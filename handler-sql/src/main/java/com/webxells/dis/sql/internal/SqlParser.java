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
package com.webxells.dis.sql.internal;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SqlParser {
    private record IgnoreRange(boolean escapable, String start, String end) {}

    private static final List<IgnoreRange> IGNORE_RANGES = List.of(
            new IgnoreRange(true, "\"", "\""),
            new IgnoreRange(true, "'", "'"),
            new IgnoreRange(true, "`", "`"),
            new IgnoreRange(false, "#", "\n"),
            new IgnoreRange(false, "--", "\n"),
            new IgnoreRange(false, "/*", "*/"));

    private final String sql;
    private final String replacedSql;
    private final int sqlLength;
    private final List<String> parameterNames = new ArrayList<>();
    private final Map<Integer, Integer> parameterReplacement = new LinkedHashMap<>();

    private Integer currentParameterStartIndex;
    private IgnoreRange currentIgnoreRange;

    public SqlParser(final String sql) {
        this.sql = sql;
        sqlLength = sql.length();
        parseSql();
        replacedSql = replaceParameter();
    }

    public List<String> getParameterNames() {
        return parameterNames;
    }

    private String replaceParameter() {
        String result = sql;
        int diff = 0;
        for (final Map.Entry<Integer, Integer> entry : parameterReplacement.entrySet()) {
            final int start = entry.getKey();
            final int end = entry.getValue();
            result = String.format("%s?%s",
                    result.substring(0, start - diff - 1),
                    result.substring(end - diff));
            diff+= end - start;
        }
        return result;
    }

    private void parseSql() {
        for (int i = 0; i < sqlLength; i++) {
            currentIgnoreRange = setIgnoreRangeIfValidates(i);
            if (null == currentIgnoreRange) {
                handleCurrentChar(i);
            } else if (null != currentParameterStartIndex) {
                saveParameterInside(i);
            }
        }
        if (null != currentParameterStartIndex && sqlLength > currentParameterStartIndex) {
            saveParameterInside(sqlLength);
        }
    }

    private void saveParameterInside(final int position) {
        parameterNames.add(sql.substring(currentParameterStartIndex, position));
        parameterReplacement.put(currentParameterStartIndex, position);
        currentParameterStartIndex = null;
    }

    private void handleCurrentChar(final int position) {
        final char current = sql.charAt(position);
        if (current == '?') {
            parameterNames.add(null);
        } else if (current == ':'
                && (0 == position || sql.charAt(position - 1) != ':')
                && (sql.length() -1 == position || sql.charAt(position + 1) != ':')) {
            currentParameterStartIndex = position + 1;
        } else {
            if (null != currentParameterStartIndex && !isValidNameRange(current)) {
                saveParameterInside(position);
            }
        }
    }

    private IgnoreRange setIgnoreRangeIfValidates(final int position) {
        if (null == currentIgnoreRange) {
            return IGNORE_RANGES.stream()
                    .filter(a -> isValidStringOnCurrentPosition(a.start, position, a.escapable))
                    .findAny()
                    .orElse(null);
        }
        if (isValidStringOnCurrentPosition(currentIgnoreRange.end, position, currentIgnoreRange.escapable)) {
            return null;
        }
        return currentIgnoreRange;
    }

    private boolean isValidStringOnCurrentPosition(final String string, int position, final boolean escapable) {
        final int stringLength = string.length();
        if (sqlLength > position + stringLength &&
                string.equals(sql.substring(position, position + stringLength))) {
            boolean result = true;
            while (escapable && --position > 0 && '\\' == sql.charAt(position)) {
                result = !result;
            }
            return result;
        }
        return false;
    }


    private boolean isValidNameRange(final char current) {
        return current == '_' ||
                (current > 47 && current < 58) ||
                (current > 64 && current < 91) ||
                (current > 96 && current < 123);
    }


    public String getSql() {
        return replacedSql;
    }
}