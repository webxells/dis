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

import com.webxells.dis.base.config.SimpleMappingPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeAssertionsTest extends ValidatorsTest {

    @Test
    void testString() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.STRING);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(" asd"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0.123"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(""), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("\n"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("\t"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(""), new SimpleMappingPart(null)));
    }

    @Test
    void testBoolean() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.BOOLEAN);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece("TrUe"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("falsE"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(" TRUE"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("FALSE "), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("1"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(" 0"), new SimpleMappingPart(null)));
    }

    @Test
    void testRespectWhitespaces() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.NUMERIC);
        fixture.setRespectWhitespaces(true);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("-" + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("+ " + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(" - " + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0.3434"), new SimpleMappingPart(null)));
        fixture.setNot(true);
        assertTrue(fixture.validate(createDatasetPiece("0,3434"), new SimpleMappingPart(null)));
    }

    @Test
    void testNumeric() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.NUMERIC);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("-" + random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("+ " + random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(" - " + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0.3434"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0,3434"), new SimpleMappingPart(null)));
    }

    @Test
    void testPositiveNumeric() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.POSITIVE_NUMERIC);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("+" + random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("+ " + random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(" + " + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("-" + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("- " + random()), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0.3434"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0,3434"), new SimpleMappingPart(null)));
    }

    @Test
    void testDecimal() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.DECIMAL);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("%s.%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("%s,%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("- %s,%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("+%s,%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format(" - %s, %s",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(String.format(" - %s, %s.",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0,"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(",0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0."), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(".0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(""), new SimpleMappingPart(null)));
    }

    @Test
    void testPositiveDecimal() {
        TypeAssertions fixture = new TypeAssertions();
        fixture.setAssertionType(TypeAssertions.Type.POSITIVE_DECIMAL);
        testNullFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece(random()), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("%s.%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("%s,%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format("+ %s,%s",random(), random())), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece(String.format(" + %s, %s",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(String.format("- %s,%s",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(String.format("-%s,%s",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(String.format(" - %s, %s",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(String.format(" - %s, %s.",random(), random())), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0,"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(",0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0."), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(".0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("asd"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece(""), new SimpleMappingPart(null)));
    }

}