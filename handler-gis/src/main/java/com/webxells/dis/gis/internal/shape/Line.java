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
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class Line extends PolyLine {
    public Line(final List<Coordinate> coordinates) {
        super(List.of(coordinates));
    }

    @Override
    public String toWktString() {
        return String.format("LINESTRING (%s)", parseLineCoordinates(coordinates.getFirst()));
    }

    @Override
    public Line transform(final CoordinateReferenceSystem outputSrs) {
        return new Line(transformCoordinates(coordinates.getFirst(), outputSrs));
    }
}