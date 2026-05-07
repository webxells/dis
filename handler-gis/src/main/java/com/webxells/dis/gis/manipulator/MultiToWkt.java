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
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;

@Description("Parses coordinates of subData (containing definition.source) to WKT (Multi-)PolygonParser")
public class MultiToWkt implements Manipulator {
    @Default("Current mapping part")
    @Description("Mapping part with coordinates in subData")
    private MappingPortrayal root;
    @Required
    private PointDefinition definition;
    /**
     * @todo: if enabled check if ring[n].coordinate[0] (n > 0) inside ring[0]
     */
    @Default("false")
    @Description("Not yet implemented")
    private boolean polygonsCouldHaveRings = false;

    @Override
    public void validate() throws InvalidApi {
        if (null == definition) {
            throw new InvalidApi("No definition for data provided");
        }
        if (polygonsCouldHaveRings) {
            throw new InvalidApi("polygonsCouldHaveRings - not yet implemented");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        try {
            currentPiece.rewriteValue(parseToWkt(Optional.ofNullable(root)
                            .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                                    .orElse(mappingPart).getSubData()));
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("Invalid polygon data detected");
        }
    }

    private String parseToWkt(final List<MappingConfiguration> subData) throws GisTransformationException {
        final boolean isMultiPolygon = 1 < subData.size();
        final StringBuilder result = new StringBuilder();
        for (MappingConfiguration a : subData) {
            Optional<MappingPart> byPortrayal = a.getByPortrayal(definition.getSource());
            if (byPortrayal.isPresent()) {
                if (0 < result.length() && isMultiPolygon) {
                    result.append(")),((");
                }
                result.append(parseData(byPortrayal.get()));
            }
        }
        result.append(isMultiPolygon ? ")))" : "))");
        return (isMultiPolygon ? "MULTIPOLYGON (((" : "POLYGON ((") + result.toString();
    }

    private String parseData(final MappingPart part) throws GisTransformationException {
        final StringBuilder result = new StringBuilder();
        Matcher matcher = definition.getPointType().getPattern().matcher(part.value().orElse(""));
        while (matcher.find()) {
            if (result.length() > 0) {
                result.append(",");
            }
            Xy input = XyParser.input(definition.getPointType(), matcher.group(), definition.getSpatialReferenceSystem());
            if (!definition.getSpatialReferenceSystem().equals(definition.getOutputSpatialReferenceSystem())) {
                input = input.transform(definition.getOutputSpatialReferenceSystem());
            }
            result.append(XyParser.output(PointType.WITH_SPACE, input));
        }
        return result.toString();
    }

    @Override
    public String getType() {
        return MultiToWkt.class.getName();
    }

    public void setDefinition(final PointDefinition definition) {
        this.definition = definition;
    }

    public void setRoot(final MappingPortrayal root) {
        this.root = root;
    }
}