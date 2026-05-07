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
package com.webxells.dis.info.input;

import com.webxells.dis.api.config.description.*;

import java.util.List;
import java.util.Map;

public class TestDisClassConfig extends TestSuperDisClassConfig {

    public enum Status {
        @Description("test on") ON,
        @Description("test off") OFF
    }

    @Required(or = {"status", "internalText"})
    @Default("1")
    public Integer amount = 1;
    @Required
    @Alias("testNumbers")
    private List<Long> numbers;
    private Map<String, TestDisClass> childMapping;

    @Required(ifPresent = {"amount"})
    private Status status;

    private String internalText;

    public void setAmount(final Integer amount) {
        this.amount = amount;
    }
    public void setNumbers(final List<Integer> numbers) {
        convertIntegerToLong();
    }

    private void convertIntegerToLong() {}

    public void setChildMapping(final Map<String, TestDisClass> childMapping) {
        this.childMapping = childMapping;
    }

    public void setStatus(final Status status) {
        this.status = status;
    }

    @Internal
    public void setInternalText(final String internalText) {
        // tests if internal setters are ignored
    }

    public void setContent(final Object content) {
        // tests if methods with parametertype Object are ignored
    }


}