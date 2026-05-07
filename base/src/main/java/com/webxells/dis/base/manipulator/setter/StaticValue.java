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
package com.webxells.dis.base.manipulator.setter;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.stream.Collectors;

@Description("Overwrites current data with new value")
public class StaticValue implements OverruleSetter {
    @Required(xor = {"values"})
    private String value;
    @Required(xor = {"value"})
    private List<String> values;

    @Override
    public String getValue(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        if (null == value && null != values) {
            mappingPart.getDataset().clear();
            mappingPart.getDataset().collect(values.stream()
                    .map(SimpleDatasetPiece::new)
                    .collect(Collectors.toList()));
            return null;
        }
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }

    @Override
    public String getType() {
        return StaticValue.class.getName();
    }

    public void setValues(final List<String> values) {
        this.values = values;
    }
}
