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
package com.webxells.dis.gis.internal.shape;

import com.webxells.dis.gis.coordinate.Coordinate;
import java.util.List;
import java.util.stream.Collectors;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class MultiPoint implements Shape {
    private final List<Coordinate> coordinates;

    public MultiPoint(final List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toWktString() {
        return String.format("MULTIPOINT (%s)", getCoordinateString());
    }

    private String getCoordinateString() {
        return coordinates.stream()
                .map(a -> String.format("(%s)", a.toWktString()))
                .collect(Collectors.joining(", "));
    }

    @Override
    public MultiPoint transform(final CoordinateReferenceSystem outputSrs) {
        return new MultiPoint(transformCoordinates(coordinates, outputSrs));
    }

    private List<Coordinate> transformCoordinates(final List<Coordinate> coordinates, final CoordinateReferenceSystem outputSrs) {
        return coordinates.stream()
                .map(a -> a.transform(outputSrs))
                .toList();
    }
}