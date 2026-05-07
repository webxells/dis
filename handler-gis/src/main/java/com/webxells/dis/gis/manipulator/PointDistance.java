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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Optional;

@Description("Calculates simple distance of two points")
public class PointDistance implements SingleCallForAllValuesManipulator {
    @Default("current mapping point")
    private MappingPortrayal pointStart;
    @Required
    private MappingPortrayal pointEnd;
    @Default("WITH_SPACE")
    private PointType pointType = XyParser.DEFAULT_POINT_TYPE;

    @Override
    public void validate() throws InvalidApi {
        if (null == pointEnd) {
            throw new InvalidApi("end point required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        try {
            currentPiece.rewriteValue(calcDistanceOfTwoPoints(mappingPart));
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("Invalid dataset detected", e);
        }
    }

    private String calcDistanceOfTwoPoints(final MappingPart part) throws GisTransformationException, InvalidDatasetException {
        final MappingConfiguration configuration = part.getConfiguration();
        final Xy p1 = getPoint(Optional.ofNullable(pointStart)
                .flatMap(configuration::getByPortrayal)
                .orElse(part));
        final Xy p2 = getPoint(configuration.getByPortrayal(pointEnd)
                .orElseThrow(() -> new InvalidDatasetException("no end point found")));
        return XyParser.singleOutput(Math.sqrt(Math.pow(p2.x() - p1.x(), 2.0) + Math.pow(p2.y() - p1.y(), 2.0)));
    }

    private Xy getPoint(final MappingPart mappingPart) throws GisTransformationException, InvalidDatasetException {
        final Optional<String> value = mappingPart.value();
        if (value.isPresent()) {
            return XyParser.input(pointType, value.get());
        }
        throw new InvalidDatasetException("no point present");
    }

    public void setPointStart(final MappingPortrayal pointStart) {
        this.pointStart = pointStart;
    }

    public void setPointEnd(final MappingPortrayal pointEnd) {
        this.pointEnd = pointEnd;
    }

    public void setPointType(final PointType pointType) {
        this.pointType = pointType;
    }
}