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
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Optional;

@Description("Creates subData with point parts")
public class SplitPoint implements SingleCallForAllValuesManipulator {
    @Description("Type of the point to split")
    @Default("WITH_SPACE")
    private PointType pointType = XyParser.DEFAULT_POINT_TYPE;
    @Default("x")
    @Description("Name of x part")
    private String nameX = "x";
    @Default("y")
    @Description("Name of y part")
    private String nameY = "y";

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        try {
            mappingPart.getDataset().clear();
            Xy point = XyParser.input(pointType, currentPiece.value().orElse(""));
            SimpleMappingConfiguration subConfiguration = new SimpleMappingConfiguration();
            subConfiguration.addPart(setValue(createPart(subConfiguration, mappingPart, nameX), point.x()));
            subConfiguration.addPart(setValue(createPart(subConfiguration, mappingPart, nameY),  point.y()));
            mappingPart.getSubData().clear();
            mappingPart.getSubData().add(subConfiguration);
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("Could not parse point", e);
        }
    }

    @Override
    public String getType() {
        return SplitPoint.class.getName();
    }

    public void setNameX(final String nameX) {
        this.nameX = nameX;
    }

    public void setNameY(final String nameY) {
        this.nameY = nameY;
    }

    public void setPointType(final PointType pointType) {
        this.pointType = pointType;
    }

    private MappingPart setValue(final MappingPart part, final double value) {
        part.getDataset().collect(new SimpleDatasetPiece(XyParser.singleOutput(value)));
        return part;
    }

    protected MappingPart createPart(final MappingConfiguration subConfiguration, final MappingPart mappingPart, final String field) {
        return new SimpleMappingPart(subConfiguration,
                new SimpleMappingPoint(getReference(mappingPart.getInput(), mappingPart.getOutput()), field),
                new SimpleMappingPoint(getReference(mappingPart.getOutput(), mappingPart.getInput()), field));
    }

    private String getReference(final MappingPoint point, final MappingPoint fallback) {
        return Optional.ofNullable(point)
                .orElse(fallback)
                .getReference();
    }
}