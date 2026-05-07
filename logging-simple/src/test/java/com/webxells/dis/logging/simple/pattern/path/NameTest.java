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
package com.webxells.dis.logging.simple.pattern.path;

import com.webxells.dis.logging.simple.Event;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NameTest {
    private class With {
        private final String name;
        private final String pattern;

        private With(String name) {
            this(name, null);
        }

        private With(String name, String pattern) {
            this.name = name;
            this.pattern = pattern;
        }

        private With test(String expected, int maxSize) {
            assertEquals(expected, parse(maxSize, name, pattern));
            return this;
        }


    }

    @Test
    void test() {
        new With("com.webxells.dis.logging.simple.pattern.path.NameTest")
                .test("NameTe", 6)
                .test("NameTest", 12)
                .test("c.w.d.l.s.p.p.NameTe", 20)
                .test("c.w.d.l.s.p.p.NameTest", 22)
                .test("c.webxe.d.logg.sim.patt.p.NameTest", 36)
                .test("c.webxel.d.loggi.simp.patte.pa.NameTest", 40)
                .test("co.webxell.di.loggin.simpl.patter.pat.NameTest", 50)
                .test("co.webxell.di.loggin.simpl.patter.pat.NameTest", 51)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 53)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 0);

        new With("com.webxells.dis.logging.simple.pattern.path.NameTest", "NO_PRIORITY")
                .test("NameTe", 6)
                .test("NameTest", 12)
                .test("c.w.d.l.s.p.p.NameTe", 20)
                .test("c.w.d.l.s.p.p.NameTest", 22)
                .test("c.webxe.d.logg.sim.patt.p.NameTest", 36)
                .test("c.webxel.d.loggi.simp.patte.pa.NameTest", 40)
                .test("co.webxell.di.loggin.simpl.patter.pat.NameTest", 50)
                .test("co.webxell.di.loggin.simpl.patter.pat.NameTest", 51)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 53)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 0);

        new With("abcdefghijklmnopqrstuvwxyz")
                .test("abcde", 5)
                .test("abcdefghijklmnopqrst", 20)
                .test("abcdefghijklmnopqrstuvwxy", 25)
                .test("abcdefghijklmnopqrstuvwxyz", 26)
                .test("abcdefghijklmnopqrstuvwxyz", 0);
    }

    @Test
    void testFirstFirst() {
        new With("com.webxells.dis.logging.simple.pattern.path.NameTest", "FIRST_FIRST")
                .test("NameTe", 6)
                .test("NameTest", 12)
                .test("c.w.d.l.s.p.p.NameTe", 20)
                .test("c.w.d.l.s.p.p.NameTest", 22)
                .test("c.w.d.l.simple.pattern.path.NameTest", 36)
                .test("c.w.d.l.simple.pattern.path.NameTest", 40)
                .test("c.w.dis.logging.simple.pattern.path.NameTest", 50)
                .test("c.webxells.dis.logging.simple.pattern.path.NameTest", 51)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 53)
                .test("com.webxells.dis.logging.simple.pattern.path.NameTest", 0);

        new With("abcdefghijklmnopqrstuvwxyz", "FIRST_FIRST")
                .test("abcde", 5)
                .test("abcdefghijklmnopqrst", 20)
                .test("abcdefghijklmnopqrstuvwxy", 25)
                .test("abcdefghijklmnopqrstuvwxyz", 26)
                .test("abcdefghijklmnopqrstuvwxyz", 0);
    }

    private String parse(final int i, String name, String pattern) {
        return new Name("maxSize:".concat(String.valueOf(i).concat(null == pattern ? "" : ",pattern:".concat(pattern))))
                .parse(new Event(null, null, name, null, null));
    }

}