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
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.lang.Math;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

@Description("Splits value into separated values")
public class SplitByLength implements SingleCallForAllValuesManipulator {
    @Required
    @Description("Length to split by")
    private int length;

    @Override
    public void validate() throws InvalidApi {
        if (0 >= length) {
            throw new InvalidApi("length is required to be greater than 0");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final List<DatasetPiece> value = new LinkedList<>(mappingPart.getDataset().getContent());
        if (!value.isEmpty()) {
            mappingPart.getDataset().clear();
            value.stream()
                    .flatMap(a -> a.value().stream())
                    .flatMap(this::splitByLength)
                    .map(SimpleDatasetPiece::new)
                    .forEach(a -> mappingPart.getDataset().collect(a));
        }
    }

    private Stream<String> splitByLength(final String text) {
        final int textLength = text.length();
        final int max = (int) Math.ceil(((double) textLength) / length);
        final List<String> result = new ArrayList<>(max);
        for (int i = 0, m = max * length; i < m; i+=length) {
            result.add(text.substring(i, Math.min(i + length, textLength)));
        }
        return result.stream();
    }

    public void setLength(final int length) {
        this.length = length;
    }
}