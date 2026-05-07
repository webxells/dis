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
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.memory.registry.VariableRegistry;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CurrentValuesTest extends SimpleTestCase {
    @Test
    void test() throws InvalidApi {
        String name = random();
        String value1 = random();
        String value2 = random();

        CurrentValues fixture = new CurrentValues(null);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setName(name);
        fixture.validate();
        assertTrue(fixture.getDataset().getContent().isEmpty());

        VariableRegistry.set(name, List.of(value1, value2));
        assertEquals(2, fixture.getDataset().getContent().size());
        assertEquals(value1, fixture.getDataset().getContent().get(0).value().get());
        assertEquals(value2, fixture.getDataset().getContent().get(1).value().get());
        assertEquals(value1, fixture.value().get());
        fixture.setMultiToSingleSelectStrategy(MappingPart.MultiToSingleSelectStrategy.LAST);
        assertEquals(value2, fixture.value().get());

        MappingPart copy = fixture.copy();
        VariableRegistry.set(name, null);
        assertTrue(fixture.getDataset().getContent().isEmpty());
        assertEquals(value2, copy.value().get());
    }

}