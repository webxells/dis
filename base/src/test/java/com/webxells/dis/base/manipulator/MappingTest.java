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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MappingTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, InvalidDatasetException {
        String defaultValue = random("default");
        String mapping1Old = random("M1old");
        String mapping1New = random("M1new");
        String mapping2Old = random("M2old");
        String mapping2New = random("M2new");
        Mapping fixture = new Mapping();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setIgnoreCase(true);
        fixture.setDefaultValue(defaultValue);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setValueMapping(Map.of(
                mapping1Old, mapping1New,
                mapping2Old, mapping2New
        ));
        fixture.validate();
        assertValue(mapping1Old.toLowerCase(), mapping1New, fixture, false);
        assertValue(mapping2Old.toLowerCase(), mapping2New, fixture, false);
        fixture.setIgnoreCase(false);
        assertValue(mapping1Old.toLowerCase(), mapping1New, fixture, true);
        assertValue(mapping2Old.toLowerCase(), mapping2New, fixture, true);
    }

    private void assertValue(String mapping1Old, String mapping1New, Mapping fixture, boolean not) throws InvalidDatasetException {
        DatasetPiece ds = new SimpleDatasetPiece(mapping1Old);
        assertEquals(mapping1Old, ds.value().get());
        fixture.manipulate(ds, null);
        if (not) {
            assertNotEquals(mapping1New, ds.value().get());
        } else {
            assertEquals(mapping1New, ds.value().get());
        }
    }

}