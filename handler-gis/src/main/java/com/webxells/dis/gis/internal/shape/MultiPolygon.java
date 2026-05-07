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
import com.webxells.dis.gis.internal.shape.parser.PolygonParser.PolygonContainer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class MultiPolygon implements Shape {
    protected final List<PolygonContainer> coordinates;

    public MultiPolygon(final List<PolygonContainer> coordinates) {
        this.coordinates = coordinates;
    }

    @Override
    public String toWktString() {
        return String.format("MULTIPOLYGON (%s)", coordinatesToString());
    }

    protected String polygonToString(PolygonContainer polygonContainer) {
        return String.format("(%s)%s", toString(polygonContainer.outerRing()),
                Optional.of(this.ringsToString(polygonContainer.innerRings()))
                        .filter(a -> !a.isBlank())
                        .map(","::concat)
                        .orElse(""));
    }

    private String coordinatesToString() {
        return coordinates.stream()
                .map(a -> String.format("(%s)", polygonToString(a)))
                .collect(Collectors.joining(", "));
    }

    private String ringsToString(final List<List<Coordinate>> lists) {
        return lists.stream()
                .map(a -> String.format("(%s)", toString(a)))
                .collect(Collectors.joining(", "));
    }

    private String toString(final List<Coordinate> coordinates) {
        return coordinates.stream()
                .map(Coordinate::toWktString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public MultiPolygon transform(final CoordinateReferenceSystem outputSrs) {
        return new MultiPolygon(coordinates.stream()
                .map(polygonContainer -> transformPolygon(polygonContainer, outputSrs))
                .toList());
    }

    protected PolygonContainer transformPolygon(PolygonContainer polygonContainer, final CoordinateReferenceSystem outputSrs) {
        return new PolygonContainer(transformList(polygonContainer.outerRing(), outputSrs),
                polygonContainer.innerRings().stream()
                        .map(b -> transformList(b, outputSrs))
                        .toList());
    }

    private List<Coordinate> transformList(final List<Coordinate> coordinates, final CoordinateReferenceSystem outputSrs) {
        return coordinates.stream()
                .map(a -> a.transform(outputSrs))
                .toList();
    }
}