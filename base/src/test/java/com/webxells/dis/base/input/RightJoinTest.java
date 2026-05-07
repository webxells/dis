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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.error.DisException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.base.config.StableMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.example.input.TestDefinitionInputConfiguration;
import com.webxells.dis.test.example.input.TestInput;
import com.webxells.dis.test.example.input.TestInputConfig;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RightJoinTest extends SimpleTestCase {

    @Test
    void test() throws DisException {
        String rootName = random("rootName");
        String name = random("name");
        TestDefinitionInputConfiguration childConfig = new TestDefinitionInputConfiguration();
        childConfig.setName(name);
        childConfig.setData(new LinkedList<>(List.of(
                Map.of("sub", random("read01")),
                Map.of("sub", random("read02")),
                Map.of(),
                Map.of("sub", random("read11")),
                Map.of("sub", random("read12")))));
        InputConfig root = new TestInputConfig(rootName);
        SimpleMappingConfiguration mappingConfiguration = new SimpleMappingConfiguration();
        mappingConfiguration.setParts(List.of(
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(rootName, "root"),
                        new SimpleMappingPoint(rootName, "root")),
                new StableMappingPart(mappingConfiguration, new SimpleMappingPoint(name, "sub"),
                        new SimpleMappingPoint(name, "sub"))
        ));
        List<Map<String, DatasetPiece>> testData = List.of(
                Map.of("root", new SimpleDatasetPiece(random("value0"))),
                Map.of("root", new SimpleDatasetPiece(random("value1"))));
        TestInput.setData(testData);
        RightJoinConfiguration configuration = new RightJoinConfiguration();
        configuration.setChildConfig(childConfig);
        configuration.setName(name);
        configuration.setRootConfig(root);

        RightJoin fixture = new RightJoin(configuration);
        assertEquals(name, fixture.getName());

        fixture.start();
        assertNull(childConfig.logger().pop());
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(testData.get(0).get("root").value().get(), mappingConfiguration.parts().get(0).value().get());
        assertEquals("START",childConfig.logger().pop());
        assertEquals("READ_SUCCEEDED",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(testData.get(0).get("root").value().get(), mappingConfiguration.parts().get(0).value().get());
        assertEquals("HAS_NEXT",childConfig.logger().pop());
        assertEquals("READ_SUCCEEDED",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(testData.get(1).get("root").value().get(), mappingConfiguration.parts().get(0).value().get());
        assertEquals("HAS_NEXT",childConfig.logger().pop());
        assertEquals("END",childConfig.logger().pop());
        assertEquals("START",childConfig.logger().pop());
        assertEquals("READ_SUCCEEDED",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
        mappingConfiguration.clear();
        assertTrue(fixture.hasNext());
        assertEquals(2, fixture.read(mappingConfiguration));
        assertEquals(testData.get(1).get("root").value().get(), mappingConfiguration.parts().get(0).value().get());
        assertEquals("HAS_NEXT",childConfig.logger().pop());
        assertEquals("HAS_NEXT",childConfig.logger().pop());
        assertEquals("READ_SUCCEEDED",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
        assertFalse(fixture.hasNext());
        assertEquals("HAS_NEXT",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
        fixture.end();
        assertEquals("END",childConfig.logger().pop());
        assertNull(childConfig.logger().pop());
    }

}