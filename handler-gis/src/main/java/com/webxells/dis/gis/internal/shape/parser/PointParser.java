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
import com.webxells.dis.gis.coordinate.parser.CoordinateParser;
import com.webxells.dis.gis.internal.shape.Point;
import java.io.IOException;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class PointParser<T extends CoordinateParser<?>> implements ShapeParser<Point> {
    private final T coordinateParser;

    public PointParser(final T coordinateParser) {
        this.coordinateParser = coordinateParser;
    }

    @Override
    public Point parse(final BufferedByteBuffer buffer, final CoordinateReferenceSystem inputCrs) throws IOException {
        return new Point(coordinateParser.parse(buffer.littleEndian(), inputCrs));
    }
}