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

import com.webxells.dis.gis.internal.BufferedByteBuffer.BufferReader;
import java.io.IOException;

public class ShpFileHeader {
    private final ShapeType shapeType;

    public ShpFileHeader(final BufferedByteBuffer buffer) throws IOException {
        final BufferReader bigEndian = buffer.bigEndian();
        assertValidFieldCode(bigEndian.getInt());
        // skip 5 unused ints and fileLength
        bigEndian.skip(24);

        final BufferReader littleEndian = buffer.littleEndian();
        assertValidVersion(littleEndian.getInt());
        shapeType = ShapeType.map(littleEndian.getInt());

        littleEndian.skip(64); //skip bbox (x,y,z,m)

        assertRightPosition(littleEndian);
    }

    public void assertValidFieldCode(final int fieldCode) throws IOException {
        if (9994 != fieldCode) {
            throw new IOException("Invalid field code: " + fieldCode);
        }
    }

    public void assertValidVersion(final int version) throws IOException {
        if (1000 != version) {
            throw new IOException("Invalid version: " + version);
        }
    }

    public ShapeType getShapeType() {
        return shapeType;
    }

    private void assertRightPosition(final BufferReader littleEndian) throws IOException {
        final int offset = littleEndian.position();
        if (100 != offset) {
            throw new IOException("Unexpected end of header location: " + offset);
        }
    }
}