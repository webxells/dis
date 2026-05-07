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

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.test.cases.ConfigurationBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

abstract class ValidatorsTest extends SimpleTestCase {

    protected void testNullFails(NotNull fixture) {
        assertFalse(fixture.validate(null, new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(null), new SimpleMappingPart(null)));
    }

    protected void testNotSetFails(IsSet fixture) {
        assertFalse(fixture.validate(createDatasetPiece(""), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("\n"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("\t"), new SimpleMappingPart(null)));
    }

    protected void testEquals(Equals fixture) {
        String value = random();
        fixture.setValue(value);
        assertEquals(Equals.class.getName(), fixture.getType());
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(value), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("fdd"), new SimpleMappingPart(null)));
    }

    protected DatasetPiece createDatasetPiece(final String value) {
        return new SimpleDatasetPiece(value);
    }
}