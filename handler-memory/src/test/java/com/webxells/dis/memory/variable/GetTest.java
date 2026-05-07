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
package com.webxells.dis.memory.variable;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.memory.registry.VariableRegistry;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GetTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        String name = random();
        String value1 = random();
        String value2 = random();
        String default1 = random();
        String default2 = random();
        MappingPart mappingPart = new SimpleMappingPart(null);
        Get fixture = new Get();
        fixture.setName(name);
        fixture.setAppend(false);
        fixture.setFailOnNotFound(false);
        fixture.setDefaults(List.of(default1, default2));

        fixture.manipulate(null, mappingPart);
        assertEquals(default1, mappingPart.getDataset().getContent().get(0).value().get());
        assertEquals(default2, mappingPart.getDataset().getContent().get(1).value().get());

        VariableRegistry.set(name, List.of(value1, value2));

        fixture.manipulate(null, mappingPart);
        assertEquals(value1, mappingPart.getDataset().getContent().get(0).value().get());
        assertEquals(value2, mappingPart.getDataset().getContent().get(1).value().get());
    }

}