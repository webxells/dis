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
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubStringTest {
    private final SubString fixture = new SubString() {{
        setErrorStrategy(ErrorStrategy.ERROR);
    }};

    @Test
    void test() throws InvalidDatasetException {
        assertRightPiece(2, 5, "cde");
        assertRightPiece(3, -1, "defgh");
        assertRightPiece(1, 0, "bcdefghi");
        assertRightPiece(0, 0, "abcdefghi");
        assertRightPiece(-5, 0, "efghi");
        assertRightPiece(-5, -4, "e");
    }

    private void assertRightPiece(final int start, final int end, final String expected) throws InvalidDatasetException {
        fixture.setStart(start);
        fixture.setEnd(end);
        DatasetPiece dataset = new SimpleDatasetPiece("abcdefghi");
        fixture.manipulate(dataset, null);
        assertEquals(expected, dataset.value().get());
    }

}