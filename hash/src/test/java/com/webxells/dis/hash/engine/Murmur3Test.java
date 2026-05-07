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
package com.webxells.dis.hash.engine;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class Murmur3Test {

    @Test
    void test() {
        Murmur3 fixture = new Murmur3();
        fixture.setSeed(10666);
        Murmur3 fixture2 = new Murmur3();
        fixture2.setSeed(666);

        assertEquals("90914b38a5d51d3dc0f28f66ddcb09f7", fixture.convert(""));
        assertEquals("d0cbb9ae1336342221a054b6a8321bb1", fixture2.convert(""));
        assertEquals("4e5ced9799433f95c252cb80e6592022", fixture.convert(Murmur3Test.class.getName()));
        assertEquals("91d4eafa4c857d7712dcdc16ad1faa20", fixture.convert(createNTimesAbc(1)));
        assertEquals("a157d7338e075112945623ce7f7e24d", fixture.convert(createNTimesAbc(1000)));
        assertEquals("58cf8851668c996695c3dc7089d6623e", fixture.convert(createNTimesAbc(666)));
        assertEquals("62c43d437b0908608355cdb7b6d45d10", fixture.convert(createNTimesAbc(934784)));
    }

    private String createNTimesAbc(int n) {
        StringBuilder result = new StringBuilder();
        for (int i = n; i-- > 0; ) {
            for (int a = 65; a < 91; a++) {
                result.append((char) a);
            }
        }
        return result.toString();
    }
}