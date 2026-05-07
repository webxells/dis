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
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LengthLimitTest {

    @Test
    void testFailing() {
        LengthLimit fixture = new LengthLimit();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setLength(Integer.MAX_VALUE);
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setLength(Integer.MIN_VALUE);
        assertThrows(InvalidApi.class, fixture::validate);
    }

    @Test
    void test() {
        assertEquals("abcde", manipulateWith("abcdefgh", 5));
        assertEquals("asdabcde", manipulateWith("asdabcdeasdabcdeasdabcdeasdabcdeasdabcdeasdabcde", 8));
        assertEquals("abcd", manipulateWith("abcdefasdgh", 4));
        assertEquals("abcde", manipulateWith("abcdefgh", -3));
        assertEquals("asdabcdeasdabcdeasdabcdeasdabcdeasdabcdeasdabc", manipulateWith("asdabcdeasdabcdeasdabcdeasdabcdeasdabcdeasdabcde", -2));
        assertEquals("abcdefasd", manipulateWith("abcdefasdgh", -2));
        assertEquals("", manipulateWith("abcdefasdgh", -28));
    }

    private String manipulateWith(final String value, final int length) {
        LengthLimit fixture = new LengthLimit();
        fixture.setLength(length);
        DatasetPiece piece = new SimpleDatasetPiece(value);
        fixture.manipulate(piece, new SimpleMappingPart(null));
        return piece.value().orElse(null);
    }

}