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
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.Manipulator;

@Description("Limits current value to given length")
public class LengthLimit implements Manipulator {
    @Required
    @Description("Must not be zero or exceeding the Integer min or max value")
    @Default("0")
    private int length;

    @Override
    public void validate() throws InvalidApi {
        if (0 == length) {
            throw new InvalidApi("length should be nonnull");
        } else if (Integer.MAX_VALUE == length || Integer.MIN_VALUE == length) {
            throw new InvalidApi("length max/min value: +/-".concat(String.valueOf(Integer.MAX_VALUE)));
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        currentPiece.value().ifPresent(value -> {
                if (length > 0 && length < value.length()) {
                    currentPiece.rewriteValue(value.substring(0, length));
                } else if (length < 0) {
                    final int guessedLength = value.length() + length;
                    if (guessedLength > 0) {
                        currentPiece.rewriteValue(value.substring(0, guessedLength));
                    } else {
                        currentPiece.rewriteValue("");
                    }
                }
        });
    }

    public void setLength(final int length) {
        this.length = length;
    }

}