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
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrimTest {

    @Test
    void test() {
        assertEquals("asd asd", manipulateWithValue(" asd asd\n", false));
        assertEquals("asd asd", manipulateWithValue("  asd asd ", false));
        assertEquals("asd asd", manipulateWithValue("asd asd", false));
        assertEquals("asdasd", manipulateWithValue(" asd asd\n", true));
        assertEquals("asdasd", manipulateWithValue("  asd asd ", true));
        assertEquals("asdasd", manipulateWithValue("asd asd", true));
    }

    private String manipulateWithValue(String value, boolean everywhere) {
        Trim fixture = new Trim();
        fixture.setEverywhere(everywhere);
        DatasetPiece piece = new SimpleDatasetPiece( value);
        fixture.manipulate(piece, new SimpleMappingPart(null));
        return piece.value().orElse(null);
    }

}