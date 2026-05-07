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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// @TODO: move to dis-plain
@Description("Matches provided pattern to current value")
public class RegularExpression extends SimpleValidator {
    @Required
    private Pattern pattern;
    @Description("Value must not be empty")
    @Default("false")
    private boolean valueRequired = false;
    @Description("Whole string has to match the given regex")
    @Default("false")
    private boolean matchWhole = false;

    @Override
    public void validate() throws InvalidApi {
        if (null == pattern) {
            throw new InvalidApi("No pattern defined");
        }
    }

    @Override
    protected boolean validateCurrentPiece(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        return matchPattern(currentPiece);
    }

    public void setPattern(final String pattern) {
        this.pattern = Pattern.compile(pattern);
    }

    public void setValueRequired(final boolean valueRequired) {
        this.valueRequired = valueRequired;
    }

    public void setMatchWhole(final boolean matchWhole) {
        this.matchWhole = matchWhole;
    }

    private boolean matchPattern(final DatasetPiece value) {
        if (value.value().isEmpty() && valueRequired) {
            return false;
        }
        return evaluate(pattern.matcher(value.value().orElse("")));
    }

    private boolean evaluate(final Matcher matcher) {
        return matchWhole ? matcher.matches() : matcher.find();
    }
}