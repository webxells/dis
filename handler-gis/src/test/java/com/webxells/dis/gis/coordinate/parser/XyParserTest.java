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
package com.webxells.dis.gis.coordinate.parser;

import com.webxells.dis.gis.GisTransformationException;
import com.webxells.dis.gis.PointType;
import com.webxells.dis.gis.coordinate.Xy;
import com.webxells.dis.gis.coordinate.parser.XyParser;
import java.util.Locale;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class XyParserTest {
    private final Locale l = Locale.US;

    @Test
    void failing() {
        assertThrows(GisTransformationException.class, () -> XyParser.input(PointType.WITH_SPACE, ""));
        assertThrows(GisTransformationException.class, () -> XyParser.input(PointType.WITH_SPACE, "123,123"));
        assertThrows(GisTransformationException.class, () -> XyParser.input(PointType.WITH_SPACE, "123123"));
    }

    @Test
    void singleOutput() {
        double x = Math.random() * 99;
        Xy point = new Xy(x,Math.random() * 99, null);
        assertEquals(XyParser.output(PointType.WITH_SPACE, point).split(" ")[0], XyParser.singleOutput(x));
    }


    @Test
    void input() throws GisTransformationException {
        double x = Math.random() * 99;
        double y = Math.random() * 99;
        Xy expected = new Xy(x, y, null);
        Locale l = Locale.US;  // we use default Locale.US for double
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE, String.format(l, "%.15f %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE, String.format(l, "%.15f   %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE, String.format(l, "%.15f  %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE, String.format(l, "%.15f   %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE_SWAPPED, String.format(l, "%.15f %.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE_SWAPPED, String.format(l, "%.15f  %.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_SPACE_SWAPPED, String.format(l, "%.15f   %.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA, String.format(l, "%.15f,%.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA, String.format(l, "%.15f ,%.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA, String.format(l, "%.15f, %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA, String.format(l, "%.15f , %.15f", x, y)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA_SWAPPED, String.format(l, "%.15f,%.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA_SWAPPED, String.format(l, "%.15f, %.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA_SWAPPED, String.format(l, "%.15f ,%.15f", y, x)));
        assertEquals(expected, XyParser.input(PointType.WITH_COMMA_SWAPPED, String.format(l, "%.15f , %.15f", y, x)));
    }

    @Test
    void output() {
        double x = Math.random() * 99;
        double y = Math.random() * 99;
        Xy point = new Xy(x,y, null);
        assertEquals(String.format("%s %s", form(x), form(y)), XyParser.output(PointType.WITH_SPACE, point));
        assertEquals(String.format("%s %s", form(y), form(x)), XyParser.output(PointType.WITH_SPACE_SWAPPED, point));
        assertEquals(String.format("%s,%s", form(x), form(y)), XyParser.output(PointType.WITH_COMMA, point));
        assertEquals(String.format("%s,%s", form(y), form(x)), XyParser.output(PointType.WITH_COMMA_SWAPPED, point));
    }

    private String form(final double cord) {
        return String.format(l, "%.15f", cord).replaceAll("0+$", "");
    }

}