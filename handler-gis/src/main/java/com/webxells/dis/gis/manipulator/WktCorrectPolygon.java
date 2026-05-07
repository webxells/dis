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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;

import java.util.LinkedList;
import java.util.List;

@Description("Closes polygon rings in MultiPolygons")
public class WktCorrectPolygon implements Manipulator {

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        currentPiece.value()
                .ifPresent(a -> currentPiece.rewriteValue(correctPolygons(a)));
    }

    private String correctPolygons(final String polygons) {
        final StringBuilder result = new StringBuilder();
        final List<String> split = splitByBracket(polygons);
        for (final String part : split) {
            handlePart(result, part);
        }
        result.append(')');
        return result.toString();
    }

    private List<String> splitByBracket(final String polygons) {
        final List<String> result = new LinkedList<>();
        int last = 0;
        for (int i = 0, m = polygons.length(); i < m; i++) {
            if (polygons.charAt(i) == ')') {
                result.add(polygons.substring(last, i));
                last = i;
            }
        }
        return result;
    }

    private void handlePart(final StringBuilder result, final String part) {
        if (")".equals(part)) {
            result.append(')');
            return;
        }
        final int bracket = getLastStartingBracket(part);
        final int firstComma = part.indexOf(',', bracket);
        final String firstCoord = part.substring(bracket, firstComma);
        final int lastComma = part.lastIndexOf(',');
        final String lastCoord = part.substring(lastComma + 1);
        result.append(String.format("%s(%s%s,%s%s",
                part.substring(0, bracket - 1), firstCoord, part.substring(firstComma, lastComma),
                lastCoord, firstCoord.equals(lastCoord) ? "" : ",".concat(firstCoord)));
    }

    private int getLastStartingBracket(final String part) {
        int result = 0;
        final int max = part.length();
        while(result < max && !Character.isDigit(part.charAt(result++)));
        return result - 1;
    }

}