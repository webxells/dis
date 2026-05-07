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
import com.webxells.dis.api.error.InvalidApi;

@Description("Simple integer range incrementing after every read")
public class RangeConfig implements InputConfig {
    @Description("Reference of input")
    @Required
    private String name;
    @Description("Start (inclusive")
    @Default("0")
    private int start;
    @Description("End (inclusive) - must be equal or greater (step > 0) / lower (step < 0) than start")
    @Required
    private int end;
    @Description("Step - must not be 0")
    @Default("1")
    private int step = 1;

    @Override
    public void validate() throws InvalidApi {
        if (null == name) {
            throw new InvalidApi("Input name must be specified");
        }
        if (0 == step || (step > 0 && end < start) || (step < 0 && end > start)) {
            throw new InvalidApi("Invalid range config: Respect field descriptions!");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return Range.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getStart() {
        return start;
    }

    public void setStart(final int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(final int end) {
        this.end = end;
    }

    public int getStep() {
        return step;
    }

    public void setStep(final int step) {
        this.step = step;
    }
}