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

import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import org.junit.jupiter.api.Test;

import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoundTest {

    @Test
    void test() throws InvalidDatasetException {
        Round roundManipulator = new Round();
        testCase("1.25", 1, "1.3", roundManipulator);
        testCase("1.25", 0, "1", roundManipulator);
        testCase("0", 2, "0.00", roundManipulator);
        testCase("13.16541651", 2, "13.17", roundManipulator);
        testCase("Test", 2, "Test", Round.ErrorStrategy.IGNORE, roundManipulator);
        testCase("Test", 2, null, RoundingMode.HALF_UP, Round.ErrorStrategy.DELETE, roundManipulator, new SimpleMappingPart(null));
    }

    @Test
    void testErrorAsStrategy() {
        Round roundManipulator = new Round();
        testCaseThrowException("Test", roundManipulator, InvalidDatasetException.class);
        testCaseThrowException("1,5", roundManipulator, InvalidDatasetException.class);
    }

    private void testCaseThrowException(final String input, final Round roundManipulator, final Class<? extends Exception> expectedException) {
        SimpleDatasetPiece simpleDatasetPiece = new SimpleDatasetPiece(input);
        roundManipulator.setErrorStrategy(Round.ErrorStrategy.ERROR);
        assertThrows(expectedException, () -> roundManipulator.manipulate(simpleDatasetPiece, null));
    }

    private void testCase(final String input,
                          final int scale,
                          final String expected,
                          final RoundingMode roundingMode,
                          final Round.ErrorStrategy errorStrategy,
                          final Round roundManipulator,
                          final SimpleMappingPart simpleMappingPart) throws InvalidDatasetException {
        SimpleDatasetPiece simpleDatasetPiece = new SimpleDatasetPiece(input);

        if (simpleMappingPart != null) {
            simpleMappingPart.getDataset().getContent().add(simpleDatasetPiece);
        }

        roundManipulator.setScale(scale);
        roundManipulator.setRoundingMode(roundingMode);
        roundManipulator.setErrorStrategy(errorStrategy);
        roundManipulator.manipulate(simpleDatasetPiece, simpleMappingPart);
        assertEquals(expected, simpleDatasetPiece.value().orElse(null));
    }

    private void testCase(final String input, final int scale, final String expected, Round roundManipulator) throws InvalidDatasetException {
        testCase(input, scale, expected, RoundingMode.HALF_UP, Round.ErrorStrategy.IGNORE, roundManipulator, null);
    }

    private void testCase(final String input, final int scale, final String expected, Round.ErrorStrategy errorStrategy, Round roundManipulator) throws InvalidDatasetException {
        testCase(input, scale, expected, RoundingMode.HALF_UP, errorStrategy, roundManipulator, null);
    }

}
