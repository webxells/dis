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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.IOException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JavaInternTest extends SimpleTestCase {

    @Test
    void test() throws InvalidApi, IOException {
        JavaIntern javaIntern = new JavaIntern();
        javaIntern.setHashType("MD5");
        javaIntern.validate();
        assertEquals("a152e841783914146e4bcd4f39100686", javaIntern.convert("asdfgh"));
        assertEquals("2ffeecf9d20ed888ed6a453447feb761", javaIntern.convert(getResourceFileStream("test1")));
        javaIntern.setHashType("SHA-1");
        javaIntern.validate();
        assertEquals("7ab515d12bd2cf431745511ac4ee13fed15ab578", javaIntern.convert("asdfgh"));
        assertEquals("403df7241b69bdb914bb484c14611d85b7919284", javaIntern.convert(getResourceFileStream("test1")));



    }

}