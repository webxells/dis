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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;

@Description("Creates values in a specified range")
public class Range implements SingleCallForAllValuesManipulator {
    public enum Edges {
        @Description("Start and end values will be included")BOTH, @Description("The start value will be included")
        FROM, @Description("The end value will be included")TO, @Description("Start and end will not be included")NONE
    }

    @Required(xor = {"fromPortrayal"})
    @Description("Value to start from")
    private Integer from;
    @Required(xor = {"toPortrayal"})
    @Description("Value of the end range")
    private Integer to;
    @Description("Has to be greater than 0")
    @Default("1")
    private int step = 1;
    @Required(xor = {"from"})
    @Description("Mapping part containing the start value")
    private MappingPortrayal fromPortrayal;
    @Required(xor = {"to"})
    @Description("Mapping part containing the end value")
    private MappingPortrayal toPortrayal;
    @Description("Defines if the start or end points will be included in created values")
    @Default("NONE")
    private Edges edges = Edges.NONE;

    @Override
    public void validate() throws InvalidApi {
        if (1 > step) {
            throw new InvalidApi("step must be greater 0");
        }
        if ((null == from && null == fromPortrayal) || (null == to && null == toPortrayal)) {
            throw new InvalidApi("from or to not defined");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        mappingPart.getDataset().clear();
        final int fromRaw = portrayalOrPlain(fromPortrayal, from, mappingPart);
        final int toRaw = portrayalOrPlain(toPortrayal, to, mappingPart);
        final int increase = step * (fromRaw < toRaw ? 1 : -1);
        int i = fromRaw + resolveEdge(Edges.FROM, increase);
        final int to = toRaw + resolveEdge(Edges.TO, increase * -1);

        for (; isValid(i, to, increase); i+= increase) {
            mappingPart.getDataset().collect(new SimpleDatasetPiece(String.valueOf(i)));
        }
    }

    private int resolveEdge(final Edges edgeToResolve, final int add) {
        return edgeToResolve == edges || Edges.BOTH == edges ? 0 : add;
    }

    private boolean isValid(final int c, final int m, final int increase) {
        return  increase > 0 ? c <= m :  c >= m;
    }

    private int portrayalOrPlain(final MappingPortrayal portrayal, final Integer plain, final MappingPart mappingPart) {
        if (null != plain) {
            return plain;
        }
        return mappingPart.getConfiguration().getByPortrayal(portrayal)
                .flatMap(MappingPart::value)
                .map(Integer::parseInt)
                .orElseThrow(() -> new RuntimeException("Invalid value found"));
    }

    public void setFrom(final int from) {
        this.from = from;
    }

    public void setTo(final int to) {
        this.to = to;
    }

    public void setFromPortrayal(final MappingPortrayal fromPortrayal) {
        this.fromPortrayal = fromPortrayal;
    }

    public void setToPortrayal(final MappingPortrayal toPortrayal) {
        this.toPortrayal = toPortrayal;
    }

    public void setStep(final int step) {
        this.step = step;
    }

    public void setEdges(final Edges edges) {
        this.edges = edges;
    }
}
