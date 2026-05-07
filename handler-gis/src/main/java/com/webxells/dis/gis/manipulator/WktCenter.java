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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.MidPoint;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Optional;

@Description("Calculates center of WKT conform coordinates")
public class WktCenter implements SingleCallForAllValuesManipulator {
    @Required
    @Description("MappingPart that holds the coordinates")
    private MappingPortrayal source;
    @Description("Defines how the resulting point is formatted")
    @Default("WKT")
    private PointType outputPointType = PointType.WKT;
    @Description("Given polygon is open, meaning the beginning does not connect to the end")
    @Default("false")
    private boolean openPolygon;

    @Override
    public void validate() throws InvalidApi {
        if (null == source) {
            throw new InvalidApi("No source provided");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final Optional<MappingPart> byPortrayal = mappingPart.getConfiguration().getByPortrayal(source);
        if (byPortrayal.isPresent() && byPortrayal.get().value().isPresent()) {
            try {
                currentPiece.rewriteValue(calculateCenter(byPortrayal.get().value().get()));
            } catch (final GisTransformationException e) {
                throw new InvalidDatasetException("Could not parse coordinate(s)", e);
            }
        }
    }

    private String calculateCenter(final String wktCoordinates) throws InvalidDatasetException, GisTransformationException {
        final String type = wktCoordinates.substring(0, Math.min(12, wktCoordinates.length())).stripLeading().toLowerCase();
        Xy result;
        if (isEmpty(wktCoordinates)) {
            result = null;
        }
        else if (type.startsWith("point")) {
            result = calculateCenterOfPoint(wktCoordinates);
        }
        else if (type.startsWith("polygon")) {
            final MidPoint midPoint = new MidPoint();
            calculateCenterOfPolygon(wktCoordinates, midPoint, 7);
            result = midPoint.notFilled() ? null : midPoint.toXy();
        }
        else if (type.startsWith("multipolygon")) {
            result = calculateCenterOfMultiPolygon(wktCoordinates);
        } else {
            throw new InvalidDatasetException("Unsupported type: ".concat(type));
        }
        if (null == result) {
            return outputPointType == PointType.WKT ? "POINT EMPTY" : null;
        }
        return XyParser.output(outputPointType, result);
    }

    private boolean isEmpty(final String wktCoordinates) {
        return "empty".equalsIgnoreCase(wktCoordinates.stripTrailing().substring(wktCoordinates.length() - 5));
    }

    private Xy calculateCenterOfMultiPolygon(final String wktCoordinates) throws GisTransformationException {
        int current = wktCoordinates.indexOf('(') + 1;
        int nextStart;
        final MidPoint result = new MidPoint();
        while (-1 < (nextStart = indexOfDoubleChar(wktCoordinates, '(', current))) {
            calculateCenterOfPolygon(wktCoordinates, result, current);
            current = nextStart + 1;
        }
        return result.notFilled() ? null : result.toXy();
    }

    private int indexOfDoubleChar(final String wktCoordinates, final char search, int current) {
        final int length = wktCoordinates.length();
        int first;
        while (-1 < (first = wktCoordinates.indexOf(search, current))) {
            while (first < length && Character.isWhitespace(wktCoordinates.charAt(first++)));
            if (first >= length) {
                break;
            }
            if (search == wktCoordinates.charAt(first)) {
                return first;
            } else {
                current = first;
            }
        }
        return -1;
    }

    private void calculateCenterOfPolygon(final String wktCoordinates, final MidPoint result, final int offset) throws GisTransformationException {
        int current = wktCoordinates.indexOf('(', wktCoordinates.indexOf('(', offset) + 1) + 1;
        if (!openPolygon) {
            current = wktCoordinates.indexOf(',', current) + 1;
        }
        walkPolygon(current, wktCoordinates.indexOf(')', current), result, wktCoordinates);
    }

    private void walkPolygon(int current, final int nextClosing, final MidPoint result, final String wktCoordinates) throws GisTransformationException {
        int nextComma;
        while(-1 < (nextComma = wktCoordinates.indexOf(',', current)) && nextComma < nextClosing) {
            nextPoint(result, wktCoordinates.substring(current, nextComma));
            current = nextComma + 1;
        }
        nextPoint(result, wktCoordinates.substring(current, nextClosing));
    }

    private void nextPoint(final MidPoint result, final String wktCoordinates) throws GisTransformationException {
        final Xy nextPoint = XyParser.input(PointType.WITH_SPACE, wktCoordinates.trim());
        result.add(nextPoint);
    }

    private Xy calculateCenterOfPoint(final String wktCoordinates) throws GisTransformationException {
        return XyParser.input(PointType.WKT, wktCoordinates);
    }

    public void setSource(final MappingPortrayal source) {
        this.source = source;
    }

    public void setOutputPointType(final PointType outputPointType) {
        this.outputPointType = outputPointType;
    }

    public boolean isOpenPolygon() {
        return openPolygon;
    }

    public void setOpenPolygon(final boolean openPolygon) {
        this.openPolygon = openPolygon;
    }
}