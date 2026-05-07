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
package com.webxells.dis.gis.coordinate.parser;

import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.internal.BufferedByteBuffer;
import com.webxells.dis.gis.coordinate.Xy;
import java.io.IOException;
import java.util.regex.Matcher;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class XyParser implements CoordinateParser<Xy> {
    public static final CoordinateReferenceSystem DEFAULT_SPATIAL_REFERENCE_SYSTEM = PointDefinition.assertValidReferenceSystem("EPSG:4326");
    public static final PointType DEFAULT_POINT_TYPE = PointType.WITH_SPACE;
    private static final XyParser INSTANCE = new XyParser();

    public static XyParser instance() {
        return INSTANCE;
    }

    private XyParser() {}

    @Override
    public Xy parse(final BufferedByteBuffer.BufferReader bufferReader, final CoordinateReferenceSystem inputCrs) throws IOException {
        return new Xy(bufferReader.getDouble(), bufferReader.getDouble(), inputCrs);
    }

    @Override
    public int numbersCount() {
        return 2;
    }

    public static Xy input(final PointType type, final String value) throws GisTransformationException {
        return input(type, value, null);
    }

    public static Xy input(final PointType type, final String value, final CoordinateReferenceSystem inoutSrs) throws GisTransformationException {
        Matcher matcher = type.getPattern().matcher(value);
        if (matcher.matches()) {
            try {
                return new Xy(
                        Double.parseDouble(matcher.group(type.getXIndex())),
                        Double.parseDouble(matcher.group(type.getYIndex())),
                        inoutSrs);
            } catch (final NumberFormatException ignored) {
                throw new GisTransformationException("Could not create Point by string: ".concat(value));
            }
        } else {
            throw new GisTransformationException(String.format("\"%s\" does not match point pattern %s", value, type.toString()));
        }
    }

    public static String output(final PointType type, final Xy point) {
        return type.getOutputTemplate()
                .replace("$x", singleOutput(point.x()))
                .replace("$y", singleOutput(point.y()));
    }

    public static String singleOutput(final double singleCoordinate) {
        return Xy.NUMBER_FORMAT.format(singleCoordinate);
    }
}