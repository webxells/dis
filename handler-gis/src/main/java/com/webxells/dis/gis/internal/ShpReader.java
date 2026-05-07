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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class ShpReader {
    private final BufferedByteBuffer buffer;
    private final int minNextSize;

    public ShpReader(final File file) throws IOException {
        if (!file.exists()) {
            throw new IOException("Cannot find file: " + file);
        }
        buffer = new BufferedByteBuffer(new FileInputStream(file));
        minNextSize = new ShpFileHeader(buffer).getShapeType().getMinRecordSize();
    }

    public boolean hasNext() throws IOException {
        return buffer.minRemaining() >= minNextSize;
    }

    public ShpFileRecord read(final CoordinateReferenceSystem inputCrs) throws IOException {
        return new ShpFileRecord(buffer, inputCrs);
    }

    void close() throws IOException {
        buffer.close();
    }
}