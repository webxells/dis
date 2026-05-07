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
package com.webxells.dis.base.manipulator;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.base.config.SimpleMappingPoint;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.webxells.dis.api.MappingPortrayal.Source.OUTPUT;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ClearValuesTest extends SimpleTestCase {

    @Test
    void test() {
        MappingPart test = new SimpleMappingPart(null) {{
            getDataset().collect(new SimpleDatasetPiece("sad"));
        }};
        new ClearValues().manipulate(null, test);
        assertTrue(test.getDataset().getContent().isEmpty());
    }

    @Test
    void testPortrayal() {
        MappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("output", "part1"))
                        .withContent(random()))
                .addPart(ConfigurationBuilder.newPart()
                        .setOutput(new SimpleMappingPoint("output", "part2"))
                        .withContent(random()))
                .build();

        ClearValues fixture = new ClearValues();
        fixture.setMappingPortrayals(List.of(new SimpleMappingPortrayal(OUTPUT, "output", "part2")));

        fixture.manipulate(null, configuration.parts().get(0));
        assertFalse(configuration.parts().get(0).getDataset().getContent().isEmpty());
        assertTrue(configuration.parts().get(1).getDataset().getContent().isEmpty());
    }

}