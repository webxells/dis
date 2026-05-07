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
package com.webxells.dis.base.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

@Description("Provides randomized data as input")
public class RandomConfig implements InputConfig {
    @Required
    @Description("Reference of this handler")
    private String name;
    @Description("Upper boundary for value generation")
    @Default("4.611.686.018.427.387.903")
    private long max = Long.MAX_VALUE / 2;
    @Description("Lower boundary for value generation")
    @Default("0")
    private long min = 0;
    @Description("Defines how many random values per mapping part shall be generated")
    @Default("1")
    private int valueSize = 1;
    @Description("Generates a secure random number (RNG)")
    @Default("false")
    private boolean secureGeneration;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return Random.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long getMax() {
        return max;
    }

    public void setMax(final long max) {
        this.max = max;
    }

    public Long getMin() {
        return min;
    }

    public void setMin(final Long min) {
        this.min = min;
    }

    public int getValueSize() {
        return valueSize;
    }

    public void setValueSize(final int valueSize) {
        this.valueSize = valueSize;
    }

    public boolean isSecureGeneration() {
        return secureGeneration;
    }

    public void setSecureGeneration(final boolean secureGeneration) {
        this.secureGeneration = secureGeneration;
    }
}