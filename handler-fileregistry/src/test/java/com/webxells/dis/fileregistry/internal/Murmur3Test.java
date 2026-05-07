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
package com.webxells.dis.fileregistry.internal;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Murmur3Test {

    @Test
    void testDoubleHashed() {
        assertEquals("90914b38a5d51d3dc0f28f66ddcb09f7e53511827ae0d0434f99ad7bc94523c6", Murmur3.doubleHashed128("", 10666));
        assertEquals("d0cbb9ae1336342221a054b6a8321bb186db0aaff58eabf4b8e29ce48c861626", Murmur3.doubleHashed128("", 666));
        assertEquals("4610abe56eff5cb551622daa78f835837d480f9fa80ec469719af4070b74d89d", Murmur3.doubleHashed128("", 1));
        assertEquals("58cf8851668c996695c3dc7089d6623ec27abfb2589da0c0d570c9bcf82d02a9", Murmur3.doubleHashed128(createNTimesAbc(666), 10666));
    }

    @Test
    void test() {
        assertEquals("90914b38a5d51d3dc0f28f66ddcb09f7", Murmur3.hash128("", 10666));
        assertEquals("d0cbb9ae1336342221a054b6a8321bb1", Murmur3.hash128("", 666));
        assertEquals("5d1dc2cf6db2c945f6c51c3d370d86ed", Murmur3.hash128(Murmur3Test.class.getName(), 10666));
        assertEquals("91d4eafa4c857d7712dcdc16ad1faa20", Murmur3.hash128(createNTimesAbc(1), 10666));
        assertEquals("a157d7338e075112945623ce7f7e24d", Murmur3.hash128(createNTimesAbc(1000), 10666));
        assertEquals("58cf8851668c996695c3dc7089d6623e", Murmur3.hash128(createNTimesAbc(666), 10666));
        assertEquals("62c43d437b0908608355cdb7b6d45d10", Murmur3.hash128(createNTimesAbc(934784), 10666));
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


