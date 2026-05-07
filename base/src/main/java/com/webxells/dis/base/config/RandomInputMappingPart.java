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
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

@Description("Mapping part using own configuration in context of base.input.Random")
public class RandomInputMappingPart extends StableMappingPart {
    @Description("Lower boundary for value generation")
    @Default("Defined in base.input.Random")
    private Long min;
    @Description("Upper boundary for value generation")
    @Default("Defined in base.input.Random")
    private Long max;
    @Description("Defines how many random values shall be generated")
    @Default("Defined in base.input.Random")
    private Integer valueSize;
    @Description("Generates a secure random number (RNG)")
    @Default("Defined in base.input.Random")
    private Boolean secureGeneration;

    public RandomInputMappingPart(final MappingConfiguration configuration) {
        super(configuration);
    }

    public RandomInputMappingPart(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    @Override
    protected MappingPart getACopy(final MappingConfiguration configuration) {
        final RandomInputMappingPart result = new RandomInputMappingPart(configuration);
        result.setMin(min);
        result.setMax(max);
        result.setValueSize(valueSize);
        result.setSecureGeneration(secureGeneration);
        return result;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(final Long min) {
        this.min = min;
    }

    public Long getMax() {
        return max;
    }

    public void setMax(final Long max) {
        this.max = max;
    }

    public Integer getValueSize() {
        return valueSize;
    }

    public void setValueSize(final Integer valueSize) {
        this.valueSize = valueSize;
    }

    public Boolean isSecureGeneration() {
        return secureGeneration;
    }

    public void setSecureGeneration(final Boolean secureGeneration) {
        this.secureGeneration = secureGeneration;
    }
}