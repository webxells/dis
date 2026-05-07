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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Optional;


@Description("Converts reference system of point definition")
public class ConvertPoint implements Manipulator {
    @Description("Holds format, source and destination of the point to convert")
    @Required
    private PointDefinition pointDefinition;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final String stringWithPoints = getRawString(currentPiece, mappingPart);
        try {
            final Xy converted = XyParser.input(pointDefinition.getPointType(), stringWithPoints, pointDefinition.getSpatialReferenceSystem())
                .transform(pointDefinition.getOutputSpatialReferenceSystem());
            writeString(XyParser.output(pointDefinition.getOutputPointType(), converted), currentPiece, mappingPart);
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("Error while converting point", e);
        }
    }

    private void writeString(final String value, final DatasetPiece currentPiece,
                             final MappingPart part) {
        if (null == pointDefinition.getDestination()) {
            currentPiece.rewriteValue(value);
        } else {
            part.getConfiguration().getByPortrayal(pointDefinition.getDestination())
                    .ifPresent(a -> a.getDataset().collect(new SimpleDatasetPiece(value)));
        }
    }

    @Override
    public String getType() {
        return ConvertPoint.class.getName();
    }

    public void setPointDefinition(final PointDefinition pointDefinition) {
        this.pointDefinition = pointDefinition;
    }

    private String getRawString(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        if (null == pointDefinition.getSource()) {
            return currentPiece.value().orElse("");
        } else {
            Optional<MappingPart> byPortrayal = mappingPart.getConfiguration().getByPortrayal(pointDefinition.getSource());
            if (byPortrayal.isPresent() && byPortrayal.get().value().isPresent()) {
                return  byPortrayal.get().value().get();
            }
        }
        return "";
    }
}