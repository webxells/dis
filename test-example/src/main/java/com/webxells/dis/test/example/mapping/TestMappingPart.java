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
package com.webxells.dis.test.example.mapping;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.base.config.StableMappingPart;

public class TestMappingPart extends StableMappingPart {
    private String test;
    public TestMappingPart(final MappingConfiguration configuration) {
        super(configuration);
    }

    public TestMappingPart(final MappingConfiguration configuration, final MappingPoint input, final MappingPoint output) {
        super(configuration, input, output);
    }

    public String getTest() {
        return test;
    }

    public void setTest(final String test) {
        this.test = test;
    }
}