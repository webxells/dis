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
package com.webxells.dis.plain.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.api.manipulator.Manipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReverseTest extends SimpleTestCase {
    @Test
    void test() {
        Reverse fixture = new Reverse();
        DatasetPiece piece = new SimpleDatasetPiece("asdfgh");
        fixture.manipulate(piece, null);
        assertEquals("hgfdsa", piece.value().get());
        fixture.manipulate(piece, null);
        assertEquals("asdfgh", piece.value().get());
        String random = random();
        piece.rewriteValue(random);
        fixture.manipulate(piece, null);
        assertEquals(new StringBuilder(random).reverse().toString(), piece.value().get());
    }
}