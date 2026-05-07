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
import com.webxells.dis.api.manipulator.Manipulator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Description("Joins values of multiple MappingParts into a single value")
public class Concatenation implements Manipulator {
    @Description("Defines MappingParts to concatenate")
    protected List<MappingPortrayal> pieces = new LinkedList<>();
    @Description("String to separate the joined values")
    protected String delimiter = "";
    @Description("Value of current MappingPart will be ignored")
    @Default("false")
    protected boolean skipSelf;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final StringBuilder startBuilder = new StringBuilder();
        if (!skipSelf) {
            writeToStart(startBuilder, currentPiece, mappingPart);
        }

        final StringBuilder pieceBuilder = new StringBuilder();
        pieces.stream()
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a).stream())
                .forEach(a -> writeToBuilder(pieceBuilder, a));
        if (startBuilder.length() > 0 && pieceBuilder.length() > 0) {
            startBuilder.append(delimiter);
        }
        startBuilder.append(pieceBuilder);

        writeManipulation(startBuilder.toString(), currentPiece, mappingPart);
    }

    protected void writeManipulation(final String newValue, final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.rewriteValue(newValue.length() > 0 ? newValue : null);
    }

    protected void writeToStart(final StringBuilder startBuilder, final DatasetPiece currentPiece, final MappingPart mappingPart) {
        startBuilder.append(currentPiece.value().orElse(""));
    }

    protected void writeToBuilder(final StringBuilder stringBuilder, final MappingPart mappingPart) {
        if (stringBuilder.length() > 0) {
            stringBuilder.append(delimiter);
        }
        stringBuilder.append(mappingPart.getDataset().getContent().stream()
                .flatMap(a -> a.value().stream())
                .collect(Collectors.joining(delimiter)));
    }

    public void setPieces(final List<MappingPortrayal> pieces) {
        this.pieces = pieces;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public void setSkipSelf(final boolean skipSelf) {
        this.skipSelf = skipSelf;
    }
}
