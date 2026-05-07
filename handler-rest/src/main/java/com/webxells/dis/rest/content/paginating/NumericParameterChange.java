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
package com.webxells.dis.rest.content.paginating;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.util.Objects;

@Description("How increment which NumericParameter")
public class NumericParameterChange implements ConfigurableByType {
    @Required
    @Description("Parameter name")
    private String name;
    @Description("Start value of the parameter")
    @Required
    private Long startValue;
    @Description("Increment value of the parameter")
    @Default("1")
    private long increment = 1;

    @Override
    public void validate() throws InvalidApi {
        if (Objects.isNull(name)) {
            throw new InvalidApi("name is required");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public long calcCurrent(final long round) {
        return startValue + (round * increment);
    }

    public long getIncrement() {
        return increment;
    }

    public void setIncrement(final long increment) {
        this.increment = increment;
    }

    public Long getStartValue() {
        return startValue;
    }

    public void setStartValue(final long startValue) {
        this.startValue = startValue;
    }
}