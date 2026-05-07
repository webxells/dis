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
package com.webxells.dis.plain.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.validator.Validator;
import com.webxells.dis.plain.intern.RegexOperation;

@Description("Validates if regex matches current value")
public class RegexMatch extends RegexOperation implements Validator {
    @Description("Index to start the regex search; only when matchWhole is false")
    @Default("0")
    private int start;
    @Description("Matches only whole text instead of substring")
    @Default("false")
    private boolean matchWhole;
    @Description("Makes validation successful, when no match was found")
    @Default("false")
    private boolean validateOnMissingValue;
    @Description("Negates result")
    @Default("false")
    private boolean not;

    @Override
    public boolean validate(final DatasetPiece datasetPiece, final MappingPart mappingPart) {
        return datasetPiece.value()
                .map(a -> getRegex(mappingPart.getConfiguration()).matcher(a))
                .map(a ->  matchWhole ? a.matches() : a.find(start))
                .map(a ->  a ^ not)
                .orElse(validateOnMissingValue);

    }

    public void setStart(final int start) {
        this.start = start;
    }

    public void setMatchWhole(final boolean matchWhole) {
        this.matchWhole = matchWhole;
    }

    public void setValidateOnMissingValue(final boolean validateOnMissingValue) {
        this.validateOnMissingValue = validateOnMissingValue;
    }

    public void setNot(final boolean not) {
        this.not = not;
    }
}
