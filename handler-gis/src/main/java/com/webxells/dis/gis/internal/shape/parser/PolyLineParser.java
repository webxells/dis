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
import com.webxells.dis.gis.internal.shape.Line;
import com.webxells.dis.gis.internal.shape.PolyLine;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class PolyLineParser<T extends CoordinateParser<?>> extends SimplePolyShapeParser<PolyLine> {
    public PolyLineParser(final T coordinateParser) {
        super(coordinateParser);
    }

    @Override
    public PolyLine parse(final BufferedByteBuffer buffer, final CoordinateReferenceSystem inputCrs) throws IOException {
        final BufferedByteBuffer.BufferReader reader = buffer.littleEndian();
        final List<List<Coordinate>> coordinates = new ArrayList<>();
        skipBBox(reader);
        parseCoordinates(coordinates, reader, inputCrs);
        return coordinates.size() == 1 ? new Line(coordinates.getFirst()) : new PolyLine(coordinates);
    }

    private void parseCoordinates(final List<List<Coordinate>> coordinates,
                                  final BufferedByteBuffer.BufferReader reader, final CoordinateReferenceSystem inputCrs) throws IOException {
        final int maxParts = reader.getInt();
        final int maxPoints = reader.getInt();
        final Queue<Integer> partIndexes = parsePartIndexes(maxParts, maxPoints, reader);
        for (int part = 0, point = 0; part < maxParts && point < maxPoints && !partIndexes.isEmpty(); part++) {
            final List<Coordinate> shapeCoordinates = new ArrayList<>();
            for (int endIndex = partIndexes.poll(); point < endIndex; point++) {
                shapeCoordinates.add(coordinateParser.parse(reader, inputCrs));
            }
            coordinates.add(shapeCoordinates);
        }
    }
}