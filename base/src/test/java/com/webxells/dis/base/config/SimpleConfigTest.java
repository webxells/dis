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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SimpleConfigTest {
    @Test
    void test() {

        SimpleMappingPoint point = new SimpleMappingPoint(null, null);
        assertNull(point.getPath());
        assertNull(point.getReference());

        SimpleJobConfig job = new SimpleJobConfig("name");
        job.setInput(null);
        assertNull(job.getInput());
        job.setOutput(null);
        assertNull(job.getOutputs());
        job.setMapping(null);
        assertNull(job.getMappings());
        job.setTrigger(null);
        assertNull(job.getTriggers());
        assertEquals("name", job.getName());

        SimpleDisConfig dis = new SimpleDisConfig();
        dis.addJobConfiguration(null);
        assertNull(dis.getConfigurations().get(0));
    }
}
