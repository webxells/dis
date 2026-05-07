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
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Description("Splits value into separated values")
public class SplitByChar implements SingleCallForAllValuesManipulator {
    @Required
    @Description("Characters to split by")
    private List<String> chars;
    @Default("false")
    private boolean isRegularExpression;

    @Override
    public void validate() throws InvalidApi {
        if (null == chars) {
            throw new InvalidApi("chars required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        final List<DatasetPiece> value = new LinkedList<>(mappingPart.getDataset().getContent());
        if (!value.isEmpty()) {
            mappingPart.getDataset().clear();
            value.stream()
                    .flatMap(a -> a.value().stream())
                    .flatMap(a -> Arrays.stream(a.split(createRegex())))
                    .map(SimpleDatasetPiece::new)
                    .forEach(a -> mappingPart.getDataset().collect(a));
        }
    }

    private String createRegex() {
        return chars.stream()
                .map(a -> isRegularExpression ? a : Pattern.quote(a))
                .collect(Collectors.joining("|"));
    }

    public void setChars(final List<String> chars) {
        this.chars = chars;
    }

    public void setRegularExpression(final boolean regularExpression) {
        isRegularExpression = regularExpression;
    }
}