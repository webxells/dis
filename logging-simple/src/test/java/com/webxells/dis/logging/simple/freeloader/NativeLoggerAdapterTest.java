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
package com.webxells.dis.logging.simple.freeloader;

import com.webxells.dis.api.Logger;
import com.webxells.dis.test.TestLogManager;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.util.logging.Level;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NativeLoggerAdapterTest extends SimpleTestCase {

    @Test
    void test() {
        String random = random();
        String random2 = random();
        String random3 = random();
        String random4 = random();
        String random5 = random();
        NativeLoggerAdapter adapter = new NativeLoggerAdapter();
        adapter.register();
        NativeLoggerAdapter.NativeLogger fixture = (NativeLoggerAdapter.NativeLogger) adapter.getLogger("test");
        final TestLogManager.TestLogger testLogger = getLogger("N.test");
        testLogger.setLevel(Logger.LogLevel.WARN);
        fixture.log(Level.WARNING, "test with freak param: {0}", random);
        fixture.log(Level.WARNING, "test %s with freak param: {}", random);
        fixture.log(Level.WARNING, "test with freak param: {}, {0}", random);
        fixture.log(Level.WARNING, "test with freak param: {4}, {3}, {2}, {}", random, random2, random3, random4, random5);

        assertEquals("test with freak param: " + random, testLogger.getEvents().get(0).message());
        assertEquals("test %s with freak param: " + random, testLogger.getEvents().get(1).message());
        assertEquals("test with freak param: " + random + ", " + random, testLogger.getEvents().get(2).message());
        assertEquals("test with freak param: " + random5 + ", " + random4 + ", " + random3 + ", " + random, testLogger.getEvents().get(3).message());
    }

}