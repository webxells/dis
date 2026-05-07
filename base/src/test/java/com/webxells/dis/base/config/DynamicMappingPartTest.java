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
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DynamicMappingPartTest extends SimpleTestCase {

    @Test
    void test() {
        DynamicMappingPart fixture = new DynamicMappingPart(null);
        MappingConfiguration configuration = newConfiguration()
                .addPart(fixture)
                .build();

        assertFalse(fixture.isStable());
        configuration.clear();
        assertTrue(configuration.parts().isEmpty());

        fixture.setStable(true);
        configuration.parts().add(fixture);

        configuration.clear();
        assertFalse(configuration.parts().isEmpty());

    }

}