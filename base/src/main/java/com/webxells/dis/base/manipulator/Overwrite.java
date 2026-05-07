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
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

@Description("Overwrites values of current MappingPart")
public class Overwrite implements SingleCallForAllValuesManipulator {
    @Description("Keeps current data and add new ones")
    @Default("false")
    private boolean append;
    @Required(xor = {"values"})
    private String value;
    @Required(xor = {"value"})
    private List<String> values;

    @Override
    public void validate() throws InvalidApi {
        if (null == value && null == values) {
            throw new InvalidApi("field value or field values required");
        }
    }

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) throws InvalidDatasetException {
        if (!append) {
            mappingPart.getDataset().clear();
        }

        Optional.ofNullable(values)
                .map(Collection::stream)
                .orElse(Stream.of(value))
                        .forEach(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }
}
