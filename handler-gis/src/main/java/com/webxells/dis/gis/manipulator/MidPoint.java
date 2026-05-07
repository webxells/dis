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
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.locationtech.proj4j.CoordinateReferenceSystem;

@Description("Calculates simple mid point of provided point definitions. " +
        "All points are required to have same spatialReferenceSystem")
public class MidPoint implements Manipulator {
    private static final Logger LOGGER = LoggerProxyFactory.logger(MidPoint.class);

    @Required
    private List<PointDefinition> points;
    private CoordinateReferenceSystem spatialReferenceSystem = XyParser.DEFAULT_SPATIAL_REFERENCE_SYSTEM;
    private PointType outputParseType = XyParser.DEFAULT_POINT_TYPE;

    @Override
    public void validate() throws InvalidApi {
        if (null == points || points.isEmpty()) {
            throw new InvalidApi("No points provided");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        try {
            currentPiece.rewriteValue(calcMidPointOfTwoPoints(mappingPart.getConfiguration()));
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("Invalid dataset detected", e);
        }
    }

    private String calcMidPointOfTwoPoints(final MappingConfiguration configuration) throws GisTransformationException {
        final AtomicReference<Xy> current = new AtomicReference<>();
        for (final PointDefinition definition : points) {
            final Optional<MappingPart> source = configuration.getByPortrayal(definition.getSource());
            if (source.isPresent() && source.get().value().isPresent()) {
                final Xy currentPoint = getCurrentPoint(definition, source.get().value().get());
                current.set(null == current.get() ? currentPoint : midpoint(current.get(), currentPoint));
            } else {
                LOGGER.debug("No point found, skipping");
            }
        }
        return null == current.get() ? "" : XyParser.output(outputParseType, current.get());
    }

    private Xy midpoint(final Xy point, final Xy currentPoint) {
        return new Xy((point.x() + currentPoint.x()) / 2, (point.y() + currentPoint.y()) / 2, null);
    }

    private Xy getCurrentPoint(final PointDefinition definition, final String value) throws GisTransformationException {
        final Xy point = XyParser.input(definition.getPointType(), value, definition.getSpatialReferenceSystem());
        return spatialReferenceSystem.equals(definition.getSpatialReferenceSystem()) ?
                point : point.transform(spatialReferenceSystem);
    }

    @Override
    public String getType() {
        return MidPoint.class.getName();
    }

    public void setPoints(final List<PointDefinition> points) {
        this.points = points;
    }

    @Default("WITH_SPACE")
    public void setOutputParseType(final PointType outputParseType) {
        this.outputParseType = outputParseType;
    }

    @Default("EPSG:4326")
    public void setSpatialReferenceSystem(final String spatialReferenceSystem) {
        this.spatialReferenceSystem = PointDefinition.assertValidReferenceSystem(spatialReferenceSystem);
    }
}