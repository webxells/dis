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
package com.webxells.dis.gis.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.error.InvalidDatasetException;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WktCorrectPolygonTest extends SimpleTestCase {

    @Test
    void test() throws InvalidDatasetException {
        test("MULTIPOLYGON (((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745)), ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746)))",
                "MULTIPOLYGON (((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745,13.583337 52.567746)), ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746)))");
        test("POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746))",
                "POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746))");
        test("POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746), (13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745))",
                "POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567746), (13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745,13.583337 52.567746))");
        test("POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745))",
                "POLYGON ((13.583337 52.567746,13.583294 52.567832,13.583294 52.567832,13.583294 52.567832,13.583337 52.567745,13.583337 52.567746))");
    }

    private void test(final String input, final String expected) throws InvalidDatasetException {
        WktCorrectPolygon fixture = new WktCorrectPolygon();
        DatasetPiece dataset = new SimpleDatasetPiece(input);
        fixture.manipulate(dataset, null);
        assertEquals(expected, dataset.value().get());
    }

}