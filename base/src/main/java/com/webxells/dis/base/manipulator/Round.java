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

import java.math.BigDecimal;
import java.math.RoundingMode;

@Description("Rounds current number")
public class Round implements Manipulator {

    public enum ErrorStrategy {
        @Description("Throws exception")
        ERROR,
        @Description("Doesn't change the current value")
        IGNORE,
        @Description("Removes current value")
        DELETE
    }

    @Description("Amount of digits after the decimal")
    @Default("0")
    private int scale = 0;
    @Description("How the rounding should behave")
    @Default("HALF_UP")
    private RoundingMode roundingMode = RoundingMode.HALF_UP;
    @Description("Handles failing during rounding")
    @Default("IGNORE")
    private ErrorStrategy errorStrategy = ErrorStrategy.IGNORE;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (!isNumeric(currentPiece.value().get())) {
            handleError(currentPiece, "Given value is not a number.", mappingPart);
        }

        currentPiece.value()
                .filter(this::isNumeric)
                .map(value -> new BigDecimal(value).setScale(scale, roundingMode))
                .ifPresent(a -> currentPiece.rewriteValue(a.toString()));
    }

    private boolean isNumeric(final String value) {
        try {
            Double.parseDouble(value);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }

        return true;
    }

    private void handleError(final DatasetPiece currentPiece, final String error, final MappingPart mappingPart) throws InvalidDatasetException {
        switch (errorStrategy) {
            case ERROR:
                throw new InvalidDatasetException(error);
            case IGNORE:
                return;
            case DELETE:
                mappingPart.getDataset().getContent().remove(currentPiece);
                currentPiece.rewriteValue(null);
        }
    }

    public void setScale(final int scale) {
        this.scale = scale;
    }

    public void setRoundingMode(final RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    public void setErrorStrategy(final ErrorStrategy errorStrategy) {
        this.errorStrategy = errorStrategy;
    }
}
