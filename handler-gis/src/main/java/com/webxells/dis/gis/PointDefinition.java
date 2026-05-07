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
package com.webxells.dis.gis;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Objects;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;
import org.locationtech.proj4j.InvalidValueException;
import org.locationtech.proj4j.UnsupportedParameterException;

@Description("Meta info required by diverse configs ")
public class PointDefinition implements ConfigurableByType  {
    private static final CRSFactory CRS_FACTORY = new CRSFactory();

    @Description("Mapping part containing coordinate(s)")
    private SimpleMappingPortrayal source;
    @Description("Mapping part to write result")
    private SimpleMappingPortrayal destination;
    @Description("Input point type")
    @Default("WITH_SPACE")
    private PointType pointType = XyParser.DEFAULT_POINT_TYPE;
    @Description("Output point type")
    private PointType outputPointType = null;
    private CoordinateReferenceSystem spatialReferenceSystem = XyParser.DEFAULT_SPATIAL_REFERENCE_SYSTEM;
    private CoordinateReferenceSystem outputSpatialReferenceSystem;

    public static CoordinateReferenceSystem assertValidReferenceSystem(final String spatialReferenceSystem) {
        try {
            return CRS_FACTORY.createFromName(Objects.requireNonNull(spatialReferenceSystem));
        } catch (final NullPointerException | UnsupportedParameterException | InvalidValueException e) {
            throw new IllegalArgumentException("Could not find defined input crs: " + spatialReferenceSystem, e);
        }
    }

    @Override
    public String getType() {
        return PointDefinition.class.getName();
    }

    public SimpleMappingPortrayal getSource() {
        return source;
    }

    public void setSource(final SimpleMappingPortrayal source) {
        this.source = source;
    }

    public PointType getPointType() {
        return pointType;
    }

    public void setPointType(final PointType pointType) {
        this.pointType = pointType;
    }

    public PointType getOutputPointType() {
        return Objects.requireNonNullElse(outputPointType, pointType);
    }

    public void setOutputPointType(final PointType outputPointType) {
        this.outputPointType = outputPointType;
    }
    public CoordinateReferenceSystem getSpatialReferenceSystem() {
        return spatialReferenceSystem;
    }

    @Description("Input spatial reference system")
    @Default("EPSG:4326")
    public void setSpatialReferenceSystem(final String spatialReferenceSystem) {
        this.spatialReferenceSystem = assertValidReferenceSystem(spatialReferenceSystem);
    }

    public CoordinateReferenceSystem getOutputSpatialReferenceSystem() {
        return Objects.requireNonNullElse(outputSpatialReferenceSystem, spatialReferenceSystem);
    }

    @Description("Output spatial reference system")
    @Default("Same as spatialReferenceSystem")
    public void setOutputSpatialReferenceSystem(final String outputSpatialReferenceSystem) {
        this.outputSpatialReferenceSystem = assertValidReferenceSystem(outputSpatialReferenceSystem);
    }

    public SimpleMappingPortrayal getDestination() {
        return destination;
    }

    public void setDestination(final SimpleMappingPortrayal destination) {
        this.destination = destination;
    }
}