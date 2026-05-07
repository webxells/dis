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
package com.webxells.dis.config.json.parsing.plugins;

import com.webxells.dis.config.json.parsing.DisonElementReader;
import com.webxells.dis.config.json.parsing.element.DisonDoubleReader;
import com.webxells.dis.config.json.parsing.element.DisonLongReader;
import com.webxells.dis.config.json.parsing.element.DisonMethodReader;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MathTest {
    @Test
    void testAddLong() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-add");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonLongReader(2),
                new DisonDoubleReader(4.0)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(6, ((DisonLongReader) actual.get()).read());
    }
    @Test
    void testMultiplyLong() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-multiply");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonLongReader(2),
                new DisonDoubleReader(4.0)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(8, ((DisonLongReader) actual.get()).read());
    }


    @Test
    void testAddDouble() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-add");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonLongReader(2),
                new DisonDoubleReader(4.4)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(6.4, ((DisonDoubleReader) actual.get()).read());
    }

    @Test
    void testSub() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-sub");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonLongReader(2),
                new DisonDoubleReader(2.0)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(0, ((DisonLongReader) actual.get()).read());
    }

    @Test
    void testSubDouble() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-sub");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonLongReader(2),
                new DisonDoubleReader(4.4)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(2-4.4, ((DisonDoubleReader) actual.get()).read());
    }

    @Test
    void testRoundDown() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-round");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonDoubleReader(4.4)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(4, ((DisonLongReader) actual.get()).read());
    }

    @Test
    void testRoundUp() {
        DisonPlugin fixture = new Math.Manager().isCompetent("--dis-round");
        DisonMethodReader method = new DisonMethodReader(fixture, List.of(new DisonDoubleReader(3.6)), null);

        Optional<DisonElementReader> actual = fixture.handle(method, null);

        assertEquals(4, ((DisonLongReader) actual.get()).read());
    }

}