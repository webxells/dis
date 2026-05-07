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
import java.util.List;

@Description("Retains all values from start plus length value")
public class ValuePick extends ValueManipulation {
    public enum ErrorStrategy {
        @Description("Raises error") ERROR, @Description("No values are picked") IGNORE
    }

    @Description("Start index to pick values from")
    @Default("0")
    private int start;
    @Description("Amount of values to pick")
    @Default("1")
    private int length = 1;
    @Description("Handles invalid start or length values")
    @Default("IGNORE")
    private ErrorStrategy errorStrategy = ErrorStrategy.IGNORE;

    @Override
    protected List<DatasetPiece> calculateNewValue(final MappingPart mappingPart) {
        final List<DatasetPiece> values = getMainPartValues(mappingPart);
        int size = values.size();
        final int indexStart = calculateNegativePosition(start, size);
        final int indexEnd = length > 0 ? indexStart + length : calculateNegativePosition(length, size + 1);
        if (validPosition(indexStart, size) && validPosition(indexEnd, size + 1)) {
            return values.subList(indexStart, indexEnd);
        }
        return List.of();
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == main) {
            throw new InvalidApi("main required");
        }
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }

    public void setLength(final int length) {
        this.length = length;
    }

    private int calculateNegativePosition(final int index, final int size) {
        return index < 0 ? size + index : index;
    }

    private boolean validPosition(final int index, final int size) {
        if (size <= index) {
            if (ErrorStrategy.ERROR.equals(errorStrategy)) {
                throw new IndexOutOfBoundsException(String.format("Calculated index %d vs. value size %d",
                        index, size));
            }
            return false;
        }
        return true;
    }

    @Override
    @Required
    public void setMain(final MappingPortrayal main) {
        super.setMain(main);
    }
}
