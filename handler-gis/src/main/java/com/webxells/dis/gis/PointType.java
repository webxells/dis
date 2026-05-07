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
package com.webxells.dis.gis;

import java.util.regex.Pattern;

public enum PointType {
    WITH_SPACE("(\\d+(\\.\\d+)?)\\s+(\\d+(\\.\\d+)?)", 1, 3,"$x $y"),
    WITH_COMMA("(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)", 1, 3,"$x,$y"),
    WITH_SPACE_SWAPPED("(\\d+(\\.\\d+)?)\\s+(\\d+(\\.\\d+)?)", 3, 1,"$y $x"),
    WITH_COMMA_SWAPPED("(\\d+(\\.\\d+)?)\\s*,\\s*(\\d+(\\.\\d+)?)", 3, 1,"$y,$x"),
    WKT(Pattern.compile("POINT\\s*\\((\\d+(\\.\\d+)?)\\s+(\\d+(\\.\\d+)?)\\s*\\)", Pattern.CASE_INSENSITIVE),
            1, 3,"POINT($x $y)");

    private final Pattern pattern;
    private final int xIndex;
    private final int yIndex;
    private final String outputTemplate;

    PointType(final String pattern, final int xIndex, final int yIndex, final String outputTemplate) {
        this(Pattern.compile(pattern), xIndex, yIndex, outputTemplate);
    }

    PointType(final Pattern pattern, final int xIndex, final int yIndex, final String outputTemplate) {
        this.pattern = pattern;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.outputTemplate = outputTemplate;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public int getXIndex() {
        return xIndex;
    }

    public int getYIndex() {
        return yIndex;
    }

    public String getOutputTemplate() {
            return outputTemplate;
        }
}
