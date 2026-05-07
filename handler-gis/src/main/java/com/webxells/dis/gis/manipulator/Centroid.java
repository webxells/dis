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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointDefinition;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.List;
import java.util.Optional;
import java.util.regex.MatchResult;
import java.util.stream.Collectors;

@Description("Creates centroid of point definition")
public class Centroid implements SingleCallForAllValuesManipulator {
    @Required
    @Description("Point definition")
    private PointDefinition definition;

    @Override
    public void validate() throws InvalidApi {
        if (null == definition) {
            throw new InvalidApi("definition required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        try {
            currentPiece.rewriteValue(calcCentroid(Optional.ofNullable(definition.getSource())
                    .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                    .orElse(mappingPart)));
        } catch (final GisTransformationException e) {
            throw new InvalidDatasetException("error on centroid calculation", e);
        }
    }

    private String calcCentroid(final MappingPart part) throws GisTransformationException {
        final List<MatchResult> results = getResultList(part);
        double sumA = 0;
        double sumX = 0;
        double sumY = 0;

        for (int i = 0, m = results.size() - 1; i <= m; i++) {
            final int nextIndex = i == m ? 0 : i + 1;
            final Xy current = XyParser.input(definition.getPointType(), results.get(i).group());
            final Xy next = XyParser.input(definition.getPointType(), results.get(nextIndex).group());
            final double shoelace = current.x() * next.y() - next.x() * current.y();
            sumA  += shoelace;
            sumX += (current.x() + next.x()) * shoelace;
            sumY += (current.y() + next.y()) * shoelace;
        }
        final double factor =  1 / (6 * (0.5 * sumA));
        return XyParser.output(definition.getOutputPointType(), new Xy(factor * sumX, factor * sumY, null));
    }

    private List<MatchResult> getResultList(final MappingPart part) throws GisTransformationException {
        final List<MatchResult> result = definition.getPointType()
                .getPattern()
                .matcher(part.value().orElse(""))
                .results()
                .collect(Collectors.toList());
        if (result.size() < 3) {
            throw new GisTransformationException("at least three coordinates are required");
        }
        return result;
    }

    public void setDefinition(final PointDefinition definition) {
        this.definition = definition;
    }
}