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

import java.util.LinkedHashMap;
import java.util.List;

@Description("Removes duplicated values")
public class ValueUnique extends ValueManipulation {

    @Override
    public void validate() { }

    @Override
    protected List<DatasetPiece> calculateNewValue(final MappingPart mappingPart) {
        final LinkedHashMap<String, DatasetPiece> result =new LinkedHashMap<>();
        getMainPartValues(mappingPart).stream()
                .filter(a -> a.value().isPresent())
                .forEach(a -> result.put(a.value().get(), a));
        return List.copyOf(result.values());
    }

}
