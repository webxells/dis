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

class ReplaceTest {

    @Test
    void regex() {
        Replace fixture = new Replace();
        fixture.setSearch("a{3}");
        fixture.setReplace("b");
        fixture.setUseRegularExpression(true);

        final SimpleDatasetPiece piece = new SimpleDatasetPiece("aaaabaaaa");
        fixture.manipulate(piece, null);

        assertEquals("babba", piece.value().get());
    }

    @Test
    void test() {
        Replace fixture = new Replace();
        fixture.setSearch("a");
        fixture.setReplace("b");

        final SimpleDatasetPiece piece = new SimpleDatasetPiece("aaaabaaaa");
        fixture.manipulate(piece, null);

        assertEquals("bbbbbbbbb", piece.value().get());

    }

}