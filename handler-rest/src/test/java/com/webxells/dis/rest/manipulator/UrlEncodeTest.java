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
package com.webxells.dis.rest.manipulator;

import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UrlEncodeTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        UrlEncode fixture = new UrlEncode();
        String testString = random("asdasd kmi-#*öä");
        SimpleDatasetPiece piece = new SimpleDatasetPiece(testString);

        fixture.manipulate(piece, null);
        assertEquals(testString
                .replace(" ", "%20")
                .replace("#", "%23")
                .replace("ö", "%C3%B6")
                .replace("ä", "%C3%A4")
                .replace("[", "%5B")
                .replace("]", "%5D"), piece.value().get());

        fixture.setStrategy(UrlEncode.Strategy.DECODE);

        fixture.manipulate(piece, null);
        assertEquals(testString, piece.value().get());
    }

}