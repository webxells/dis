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

import com.webxells.dis.base.SimpleDatasetPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChangeCaseTest {

    @Test
    void test() {
        ChangeCase fixture = new ChangeCase();
        testCase("TEST_String", "test_string", fixture);
        testCase("Üol", "üol", fixture);
        testCase("üol", "üol", fixture);
        fixture.setToType(ChangeCase.Type.UPPER);
        testCase("TEST_String", "TEST_STRING", fixture);
        testCase("Üol", "ÜOL", fixture);
        testCase("ÜOL", "ÜOL", fixture);

    }

    private void testCase(final String input, final String expected, final ChangeCase fixture) {
        SimpleDatasetPiece piece = new SimpleDatasetPiece(input);
        fixture.manipulate(piece, null);
        assertEquals(expected, piece.value().get());
    }

}