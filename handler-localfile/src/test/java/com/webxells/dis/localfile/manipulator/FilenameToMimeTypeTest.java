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
package com.webxells.dis.localfile.manipulator;

import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPart;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FilenameToMimeTypeTest extends SimpleTestCase {

    private final String randomExtension = random();
    private final String randomDefaultExtension = random();

    @Test
    void testSimpleMapping() throws Exception {
        test("text.pdf", "application/pdf");
        test("text.pDf", "application/pdf");
        test("text.PDF", "application/pdf");
        test("text.txt", "text/plain");
        test("text.csv", "text/csv");
        test("text.png", "image/png");
        test("text.rand", randomExtension, true);
        test("text.png", "image/png", true);
        test(random(), "application/octet-stream");
        test(random(), randomDefaultExtension, randomDefaultExtension);
        test("text.png", "image/png", randomDefaultExtension);
    }

    void test(final String input, final String expected) throws Exception {
        test(input, expected, false, null);
    }

    void test(final String input, final String expected, boolean enableCustomType) throws Exception {
        test(input, expected, enableCustomType, null);
    }

    void test(final String input, final String expected, String defaultType) throws Exception {
        test(input, expected, false, defaultType);
    }

    void test(final String input, final String expected, boolean enableCustomType, String defaultType) throws Exception {
        MappingPart part = new SimpleMappingPart(null);
        part.getDataset().collect(new SimpleDatasetPiece(input));
        FilenameToMimeType fixture = new FilenameToMimeType();
        if (enableCustomType) {
            fixture.setCustomTypes(Map.of("rand", randomExtension));
        }
        if (null != defaultType) {
            fixture.setDefaultType(defaultType);
        }
        fixture.manipulate(null, part);
        assertEquals(expected, part.value().get());
    }
}