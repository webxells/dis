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
package com.webxells.dis.logging.simple;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.logging.simple.appender.FileAppender;
import com.webxells.dis.logging.simple.appender.PrinterStreamAppender;
import java.io.IOException;
import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

class DisLogManagerTest {

    @Test
    void test() throws IOException {
        DisLogManager fixture = new DisLogManager(DisLogManagerTest.class.getClassLoader().getResourceAsStream("simple.json"), 50);

        assertInstanceOf(Logger.class, fixture.get("test"));
        assertInstanceOf(Logger.class, fixture.get(this.getClass()));

        assertEquals(LogLevel.WARN, fixture.getDefaultLevel());
        assertEquals("$thread - $date(pattern: YYYY-mm-dd HH:MM:ss): $msg", fixture.getDefaultPattern());

        assertEquals(2, fixture.getAppenders().size());
        assertSame(FileAppender.class, fixture.getAppenders().getFirst().getClass());
        assertEquals(LogLevel.ERROR, fixture.getAppenders().get(0).getLogLevel());
        assertEquals("$thread - $date(pattern: YYYY-mm-dd HH:MM:ss): $msg(limit:200, intelligentTabs:true, replaceSpaces:true)", fixture.getAppenders().get(0).getPattern());

        assertSame(PrinterStreamAppender.class, fixture.getAppenders().get(1).getClass());
        assertEquals(LogLevel.WARN, fixture.getAppenders().get(1).getLogLevel());
        assertEquals("$thread - $date(pattern: YYYY-mm-dd HH:MM:ss): $msg", fixture.getAppenders().get(1).getPattern());

        assertEquals(Map.of("EXCEPTION", LogLevel.FATAL, "EXCEPTION2", LogLevel.TRACE),
                fixture.getDefaultExceptions());
        assertEquals(LogLevel.INFO, fixture.get("EXCEPTION").getLevel());
        assertEquals(LogLevel.TRACE, fixture.get("EXCEPTION2").getLevel());
        assertEquals(LogLevel.INFO, fixture.get("EXCEPTION3").getLevel());
        assertEquals(LogLevel.WARN, fixture.get("3EXCEPTION").getLevel());

    }

}