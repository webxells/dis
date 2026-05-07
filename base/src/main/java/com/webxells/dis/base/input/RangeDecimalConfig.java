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

@Description("Big decimal range incrementing after every read")
public class RangeDecimalConfig implements InputConfig {
    @Description("Reference of input")
    @Required
    private String name;
    @Description("Start (inclusive")
    @Default("0.0")
    private String start = "0.0";
    @Description("End (inclusive) - must be equal or greater (step > 0) / lower (step < 0) than start")
    @Default("1.0")
    private String end = "1.0";
    @Description("Step - must not be 0")
    @Default("1.0")
    private String step = "1.0";

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getStart() {
        return start;
    }

    public void setStart(final String start) {
        this.start = start;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(final String end) {
        this.end = end;
    }

    public String getStep() {
        return step;
    }

    public void setStep(final String step) {
        this.step = step;
    }

    @Override
    public String getType() {
        return RangeDecimal.class.getName();
    }
}