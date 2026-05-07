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
package com.webxells.dis.gis.internal;

import com.webxells.dis.gis.coordinate.parser.XyParser;
import com.webxells.dis.gis.internal.shape.Shape;
import com.webxells.dis.gis.internal.shape.parser.MultiPointParser;
import com.webxells.dis.gis.internal.shape.parser.NullParser;
import com.webxells.dis.gis.internal.shape.parser.PointParser;
import com.webxells.dis.gis.internal.shape.parser.PolyLineParser;
import com.webxells.dis.gis.internal.shape.parser.PolygonParser;
import com.webxells.dis.gis.internal.shape.parser.ShapeParser;
import java.io.IOException;
import java.util.Arrays;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public enum ShapeType {
    NULL(0, 1, new NullParser()),
    POINT( 1, 20, new PointParser<>(XyParser.instance())),
    POLYLINE(3, 48, new PolyLineParser<>(XyParser.instance())),
    POLYGON(5, 48, new PolygonParser<>(XyParser.instance())),
    MULTIPOINT(8, 48, new MultiPointParser<>(XyParser.instance()));
    /*
    Z_POINT(11, new PointParser<>(XyzParser.instance()))),
    Z_POLYLINE(13),
    Z_POLYGON(15),
    Z_MULTIPOINT(18),
    M_POINT(21),
    M_POLYLINE(23),
    M_POLYGON(25),
    M_MULTIPOINT(28),
    MULTIPATCH(31); */

    public static ShapeType map(final int value) {
        return Arrays.stream(ShapeType.values())
                .filter(a -> value == a.value)
                .findAny()
                .orElseThrow(() -> new IllegalArgumentException("Unsupported shapeParser type: " + value));
    }

    private final int value;
    private final int minRecordSize;
    private final ShapeParser<?> shapeParser;

    ShapeType(final int value, final int minRecordSize, final ShapeParser<?> shapeParser) {
        this.value = value;
        this.minRecordSize = minRecordSize;
        this.shapeParser = shapeParser;
    }

    public int value() {
        return value;
    }

    public Shape parse(BufferedByteBuffer buffer, final CoordinateReferenceSystem inputCrs) throws IOException {
        return shapeParser.parse(buffer, inputCrs);
    }

    public int getMinRecordSize() {
        return minRecordSize;
    }
}