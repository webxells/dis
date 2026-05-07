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
package com.webxells.dis.time.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.time.intern.SimpleTimeApi;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Description("Converts date to another format")
public class ConvertDateFormat extends SimpleTimeApi implements Manipulator {

    @Required()
    @Description("Format of the given date to convert to")
    private String convertFormat;

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (null == convertFormat || convertFormat.isBlank()) {
            throw new InvalidApi("Required fields are missing");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        validateData(currentPiece);
        currentPiece.rewriteValue(convertToFormat(currentPiece));
    }

    private void validateData(final DatasetPiece currentPiece) throws InvalidDatasetException {
        if (null == currentPiece || currentPiece.value().isEmpty()) {
            throw new InvalidDatasetException("No data in mapping part found");
        }
    }

    private String convertToFormat(final DatasetPiece currentPiece) throws InvalidDatasetException {
        try {
            final LocalDateTime dateTime = LocalDateTime.parse(currentPiece.value().get(), format);
            return dateTime.format(DateTimeFormatter.ofPattern(convertFormat));
        } catch (final DateTimeParseException e) {
            final String errorMessage = String.format("Date %s could not be parsed with format %s",
                    currentPiece.value().get(), format);
            throw new InvalidDatasetException(errorMessage, e);
        }
    }

    public void setConvertFormat(final String convertFormat) {
        this.convertFormat = convertFormat;
    }
}
