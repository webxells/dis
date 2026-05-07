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
package com.webxells.dis.gis.internal.shape.parser;

import com.webxells.dis.gis.internal.BufferedByteBuffer;
import com.webxells.dis.gis.coordinate.Coordinate;
import com.webxells.dis.gis.coordinate.parser.CoordinateParser;
import com.webxells.dis.gis.internal.shape.MultiPolygon;
import com.webxells.dis.gis.internal.shape.Polygon;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class PolygonParser<T extends CoordinateParser<?>> extends SimplePolyShapeParser<MultiPolygon> {
    public record PolygonContainer(List<Coordinate> outerRing, List<List<Coordinate>> innerRings) {}

    public PolygonParser(final T coordinateParser) {
        super(coordinateParser);
    }

    @Override
    public MultiPolygon parse(final BufferedByteBuffer buffer, final CoordinateReferenceSystem inputCrs) throws IOException {
        final BufferedByteBuffer.BufferReader reader = buffer.littleEndian();
        skipBBox(reader);//mb save for quick access
        final List<PolygonContainer> coordinates = parseCoordinates(reader, inputCrs);
        return coordinates.size() == 1 ? new Polygon(coordinates.getFirst()) : new MultiPolygon(coordinates);
    }

    private List<PolygonContainer> parseCoordinates(final BufferedByteBuffer.BufferReader reader, final CoordinateReferenceSystem inputCrs) throws IOException {
        final List<PolygonContainer> result = new ArrayList<>();
        final int maxParts = reader.getInt();
        final int maxPoints = reader.getInt();
        final Queue<Integer> partIndexes = parsePartIndexes(maxParts, maxPoints, reader);
        List<Coordinate> outerRing = null;
        List<List<Coordinate>> innerRings = new ArrayList<>();
        for (int part = 0, point = 0; part < maxParts && point < maxPoints && !partIndexes.isEmpty(); part++) {
            final List<Coordinate> current = new ArrayList<>();
            for (int endIndex = partIndexes.poll(); point < endIndex; point++) {
                current.add(coordinateParser.parse(reader, inputCrs));
            }
            if (current.isEmpty()) {
                throw new IOException("Polygon ring must not be empty");
            }
            if (null != outerRing) {
                if (contains(outerRing, current.getFirst())){
                    innerRings.add(current);
                    continue;
                } else {
                    result.add(new PolygonContainer(outerRing, innerRings));
                    innerRings = new ArrayList<>();
                }
            }
            outerRing = current;
        }
        result.add(new PolygonContainer(outerRing, innerRings));
        return result;
    }

    private boolean contains(final List<Coordinate> outerRing, final Coordinate test) {
        final Coordinate lastPoint = outerRing.getLast();
        boolean crossedOnce = false;
        for (final Coordinate current : outerRing) {
            if (current.rayCrossingThrough(lastPoint, test)) {
                crossedOnce = !crossedOnce;
            }
        }
        return crossedOnce;
    }
}