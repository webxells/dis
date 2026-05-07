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
package com.webxells.dis.gis.coordinate;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.locationtech.proj4j.BasicCoordinateTransform;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.ProjCoordinate;

public record Xy(double x, double y, CoordinateReferenceSystem inputSrs) implements Coordinate {
    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    static {
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(16);
        NUMBER_FORMAT.setRoundingMode(RoundingMode.HALF_UP);
        NUMBER_FORMAT.setGroupingUsed(false);
    }
    private static final Map<String, BasicCoordinateTransform> TRANSFORM_MAP = new HashMap<>();

    @Override
    public String toWktString() {
        return String.format("%s %s",  NUMBER_FORMAT.format(x), NUMBER_FORMAT.format(y));
    }

    @Override
    public Xy transform(final CoordinateReferenceSystem outputSrs) {
        final BasicCoordinateTransform coordinateTransform = getCoordinateTransform(inputSrs, outputSrs);
        final ProjCoordinate result = coordinateTransform.transform(new ProjCoordinate(x, y), new ProjCoordinate());
        return new Xy(result.x,  result.y, outputSrs);
    }

    @Override
    public boolean rayCrossingThrough(final Coordinate lineTo, final Coordinate point) {
        if (lineTo instanceof Xy lineToXy && point instanceof Xy pointXy) {
            return (y > pointXy.y) != (lineToXy.y > pointXy.y) &&
                    (pointXy.x < (lineToXy.x - x) * (pointXy.y - y) / (lineToXy.y - y) + x);
        }
        return false;
    }

    @Override
    public int numbersCount() {
        return 2;
    }

    private static BasicCoordinateTransform getCoordinateTransform(final CoordinateReferenceSystem inputSrs,
                                                                   final CoordinateReferenceSystem outputSrs) {
        final String index = String.format("%s||%s", inputSrs.getName(), outputSrs.getName());
        return TRANSFORM_MAP.computeIfAbsent(index, key -> new BasicCoordinateTransform(inputSrs, outputSrs));
    }
}