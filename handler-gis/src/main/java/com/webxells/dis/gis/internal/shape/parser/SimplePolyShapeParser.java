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
import com.webxells.dis.gis.internal.shape.Shape;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

public abstract class SimplePolyShapeParser<T extends Shape> extends SimpleShapeParser<T> {

    public SimplePolyShapeParser(final CoordinateParser<?> coordinateParser) {
        super(coordinateParser);
    }

    protected Queue<Integer> parsePartIndexes(final int maxParts, final int maxPoints, final BufferedByteBuffer.BufferReader reader) throws IOException {
        final Queue<Integer> result = new LinkedList<>();
        for (int i = 0; i < maxParts; i++) {
            result.add(reader.getInt());
        }
        result.poll(); //dont need starting 0
        result.add(maxPoints);
        return result;
    }
}