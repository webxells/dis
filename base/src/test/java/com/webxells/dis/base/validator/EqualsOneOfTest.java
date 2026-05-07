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
package com.webxells.dis.base.validator;

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.config.SimpleMappingPart;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EqualsOneOfTest extends ValidatorsTest{

    @Test
    void test() {
        EqualsOneOf fixture = new EqualsOneOf();
        assertThrows(InvalidApi.class, fixture::validate);
        String value = randomUnique();
        String value2 = randomUnique();
        fixture.setValues(List.of(value, value2));
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(value), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(value2), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("\ta"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        fixture.setNot(true);
        assertFalse(fixture.validate(createDatasetPiece(value), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(value2), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("\ta"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
    }

}