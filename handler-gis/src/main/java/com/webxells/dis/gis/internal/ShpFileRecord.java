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

import com.webxells.dis.gis.internal.shape.Shape;
import java.io.IOException;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class ShpFileRecord {
    private final Shape shape;

    public ShpFileRecord(final BufferedByteBuffer buffer, final CoordinateReferenceSystem inputCrs) throws IOException {
        BufferedByteBuffer.BufferReader bigEndian = buffer.bigEndian();
        bigEndian.skip(4); // skip shp number
        final int length = bigEndian.getInt();
        shape = ShapeType.map(length > 0 ? buffer.littleEndian().getInt() : 0).parse(buffer, inputCrs);
    }

    public Shape getShape() {
        return shape;
    }

}