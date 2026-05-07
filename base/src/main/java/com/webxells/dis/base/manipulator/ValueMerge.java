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

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Description("Moves data from subsets to this Dataset")
public class ValueMerge extends ValueManipulation {

    @Override
    protected List<DatasetPiece> calculateNewValue(final MappingPart mappingPart) {
        return merge(getMainPartValues(mappingPart),
                subsets.stream()
                        .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a).stream())
                        .flatMap(a -> a.getDataset().getContent().stream())
                        .collect(Collectors.toList()));
    }

    private List<DatasetPiece> merge(final List<DatasetPiece> mainPartValues, final List<DatasetPiece> collect) {
        final List<DatasetPiece> result = new LinkedList<>();
        result.addAll(mainPartValues);
        result.addAll(collect);
        return result;
    }

}