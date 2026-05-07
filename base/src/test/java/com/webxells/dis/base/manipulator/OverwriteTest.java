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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OverwriteTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException, InvalidApi {
        Overwrite fixture = new Overwrite();
        String value = random();
        fixture.setValue(value);
        fixture.validate();

        SimpleMappingPart mappingPart = new SimpleMappingPart(null);
        fixture.manipulate(null, mappingPart);
        assertEquals(value, mappingPart.value().get());
        assertEquals(1, mappingPart.getDataset().getContent().size());

        String newValue = random();
        fixture.setValue(newValue);
        fixture.manipulate(null, mappingPart);
        assertEquals(newValue, mappingPart.value().get());
        assertEquals(1, mappingPart.getDataset().getContent().size());

        String nextValue = random();
        fixture.setValue(nextValue);
        fixture.setAppend(true);
        fixture.manipulate(null, mappingPart);
        assertEquals(newValue, mappingPart.value().get());
        assertEquals(nextValue, mappingPart.getDataset().getContent().get(1).value().get());
        assertEquals(2, mappingPart.getDataset().getContent().size());

        fixture.setAppend(false);
        final List<String> values = List.of(random(), random(), random());
        fixture.setValues(values);

        fixture.manipulate(null, mappingPart);
        assertEquals(3, mappingPart.getDataset().getContent().size());
        assertEquals(values.get(0), mappingPart.getDataset().getContent().get(0).value().get());
        assertEquals(values.get(1), mappingPart.getDataset().getContent().get(1).value().get());
        assertEquals(values.get(2), mappingPart.getDataset().getContent().get(2).value().get());
    }
}