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
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;

@Description("Calculates substring out of value")
public class SubString implements Manipulator {
    public enum ErrorStrategy {
        @Description("Throws exception")
        ERROR,
        @Description("Empties value")
        EMPTY,
        @Description("Sets value to null")
        NULL,
        @Description("Doesn't change the value")
        IGNORE
    }

    @Description("Start of substring")
    @Default("0")
    private int start;
    @Description("End of substring")
    @Default("Length of the current value")
    private int end;
    @Description("What to do when calculation fails")
    @Default("IGNORE")
    private ErrorStrategy errorStrategy = ErrorStrategy.IGNORE;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (currentPiece.value().isEmpty()) {
            handleError(currentPiece, "Dataset not present");
            return;
        }
        final String current = currentPiece.value().get();
        final int length = current.length();
        final int calculatedStart = calculateCurrentIndex(start, length, 0);
        final int calculatedEnd = calculateCurrentIndex(end, length, 1);
        if (length < calculatedEnd || calculatedStart > calculatedEnd) {
            handleError(currentPiece, "Illegal index");
            return;
        }
        currentPiece.rewriteValue(current.substring(calculatedStart, calculatedEnd));
    }

    private int calculateCurrentIndex(final int index, final int length, final int limit) {
        return index < limit ? length + index : index;
    }

    private void handleError(final DatasetPiece currentPiece, final String error) throws InvalidDatasetException {
        String value = null;
        switch (errorStrategy) {
            case ERROR:
                throw new InvalidDatasetException(error);
            case IGNORE:
                return;
            case EMPTY:
                value = "";
        }
        currentPiece.rewriteValue(value);
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }
}