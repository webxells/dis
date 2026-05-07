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
package com.webxells.dis.base.config;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.base.SimpleDatasetPiece;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("MappingPart containing fixed value(s)")
public class StaticValueMappingPart extends StableMappingPart {
    @Description("Sets single value")
    @Required(xor = "values")
    private String value;
    @Description("Sets multiple values")
    @Required(xor = "value")
    private List<String> values = List.of();

    public StaticValueMappingPart(final MappingConfiguration configuration) {
        super(configuration);
    }

    public StaticValueMappingPart(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    public void setValue(final String value) {
        this.value = value;
        setValue();
    }

    public void setValues(final List<String> values) {
        this.values = values;
        setValue();
    }

    private void setValue() {
        Optional.ofNullable(value)
                .map(List::of)
                .orElse(values).stream()
                .map(SimpleDatasetPiece::new)
                .forEach(a -> getDataset().collect(a));
    }

    @Override
    protected MappingPart getACopy(final MappingConfiguration configuration) {
        return new StaticValueMappingPart(configuration);
    }

    @Override
    public void clear() {
        super.clear();
        setValue();
    }
}