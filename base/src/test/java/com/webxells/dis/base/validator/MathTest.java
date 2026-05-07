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

import com.webxells.dis.base.SimpleMappingPortrayal;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.test.cases.ConfigurationBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MathTest extends ValidatorsTest {

    @Test
    void testModXIsZero() {
        Math fixture = createFixture(Math.Operation.MOD_IS_ZERO, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("3"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("56"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("46.00000"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("69"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("92"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testUsingPortrayalAsComparativeValue() {
        SimpleMappingConfiguration configuration = newConfiguration()
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM)
                            .withContent(random()))
                .addPart(ConfigurationBuilder.newPart(ConfigurationBuilder.PartBuilder.PartBuilderState.RANDOM))
                .build();
        Math fixture = new Math();
        fixture.setComparativePortrayal(SimpleMappingPortrayal.destination(configuration.parts().get(0)));
        fixture.setOperation(Math.Operation.GREATER);
        testNullFails(fixture);
        testNotSetFails(fixture);
        long comparativeValue = Long.parseLong(configuration.parts().get(0).value().get());
        assertTrue(fixture.validate(
                createDatasetPiece(String.valueOf(comparativeValue + 1)),
                configuration.parts().get(1)));
        assertFalse(fixture.validate(
                createDatasetPiece(String.valueOf(comparativeValue - 1)),
                configuration.parts().get(1)));
    }

    @Test
    void testModXGreaterZero() {
        Math fixture = createFixture(Math.Operation.MOD_IS_NOT_ZERO, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertFalse(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("3"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("56"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("46.00000"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("69"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("92"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testNotEquals() {
        Math fixture = createFixture(Math.Operation.NOT_EQUALS, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        testNotEqualsOperation(fixture);
        fixture.setOperation(Math.Operation.NOT_EQUALS);
        testNotEqualsOperation(fixture);
    }

    private void testNotEqualsOperation(final Math fixture) {
        assertTrue(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0023"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23.0000"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("12323.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testEquals() {
        Math fixture = createFixture(Math.Operation.EQUALS, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        testEqualsOperation(fixture);
        fixture.setOperation(Math.Operation.EQUALS);
        testEqualsOperation(fixture);
    }

    private void testEqualsOperation(final Math fixture) {
        assertFalse(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0023"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23.0000"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("12323.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testGreaterEquals() {
        Math fixture = createFixture(Math.Operation.GREATER_EQUALS, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertFalse(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("12323.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testGreater() {
        Math fixture = createFixture(Math.Operation.GREATER, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertFalse(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("12323.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testSmallerEquals() {
        Math fixture = createFixture(Math.Operation.LOWER_EQUALS, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    @Test
    void testSmaller() {
        Math fixture = createFixture(Math.Operation.LOWER, "23");
        testNullFails(fixture);
        testNotSetFails(fixture);
        assertTrue(fixture.validate(createDatasetPiece("22, 123"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("2. 34"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("0"), new SimpleMappingPart(null)));
        assertTrue(fixture.validate(createDatasetPiece("-23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23.345"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("23"), new SimpleMappingPart(null)));
        assertFalse(fixture.validate(createDatasetPiece("a"), new SimpleMappingPart(null)));
    }

    private Math createFixture(final Math.Operation operation, final String value2) {
        Math fixture = new Math();
        fixture.setComparativeValue(value2);
        fixture.setOperation(operation);
        return fixture;
    }


}