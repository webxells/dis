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
package com.webxells.dis.logging.simple.appender;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.logging.simple.Event;
import com.webxells.dis.test.cases.SimpleTestCase;
import java.io.PrintStream;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class PrinterStreamAppenderTest extends SimpleTestCase {

    @Test
    void testExceptions() {
        PrintStream printStream = mock(PrintStream.class);
        Thread thisThread = Thread.currentThread();
        PrinterStreamAppender fixture = new PrinterStreamAppender(printStream);
        fixture.setLevel(LogLevel.DEBUG);
        fixture.setPattern("$level: $msg");
        fixture.setExceptions(Map.of("EXCEPTION", LogLevel.WARN));

        fixture.write(new Event(thisThread, LogLevel.INFO, "EXCEPTION", "should-not-be-visible (match info < warn)"));
        fixture.write(new Event(thisThread, LogLevel.INFO, "EXCEPT", "should-be-visible (no match)"));
        fixture.write(new Event(thisThread, LogLevel.INFO, random(), "should-be-visible (no match2)"));
        fixture.write(new Event(thisThread, LogLevel.INFO, "EXCEPTION AND MORE", "should-not-be-visible (start with match info < warn)"));
        fixture.write(new Event(thisThread, LogLevel.WARN, "EXCEPTION AND MORE", "should-be-visible (start with match warn == warn)"));
        fixture.write(new Event(thisThread, LogLevel.FATAL, "EXCEPTION AND MORE", "should-be-visible (start with match fatal > warn)"));

        InOrder inOrder = inOrder(printStream);
        inOrder.verify(printStream).println("INFO: ".concat("should-be-visible (no match)"));
        inOrder.verify(printStream).println("INFO: ".concat("should-be-visible (no match2)"));
        inOrder.verify(printStream).println("WARN: ".concat("should-be-visible (start with match warn == warn)"));
        inOrder.verify(printStream).println("FATAL: ".concat("should-be-visible (start with match fatal > warn)"));
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void testWithBeforeAndAfter() {
        PrintStream printStream = mock(PrintStream.class);
        Thread thisThread = Thread.currentThread();
        String msgBefore3 = random("before3");
        String msgBefore2 = random("before2");
        String msgBefore1 = random("before1");
        String msgAfter1 = random("after");
        String separator = random("sep");
        String msg = random("msg");
        String name = random();
        PrinterStreamAppender fixture = new PrinterStreamAppender(printStream);
        fixture.linesAfter = 1;
        fixture.linesBefore = 2;
        fixture.separator = separator;
        fixture.setLevel(LogLevel.INFO);
        fixture.setPattern("$level: $msg");

        fixture.write(new Event(thisThread, LogLevel.INFO, name, msgBefore3));
        fixture.write(new Event(thisThread, LogLevel.TRACE, name, msgBefore2));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, random()));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, random()));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, random()));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, msgBefore1));
        fixture.write(new Event(thisThread, LogLevel.INFO, name, msg));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, msgAfter1));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, random()));
        fixture.write(new Event(thisThread, LogLevel.DEBUG, name, random()));

        InOrder inOrder = inOrder(printStream);
        inOrder.verify(printStream).println("INFO: ".concat(msgBefore3));
        inOrder.verify(printStream).println("TRACE: ".concat(msgBefore2));
        inOrder.verify(printStream).println(separator);
        inOrder.verify(printStream).println("DEBUG: ".concat(msgBefore1));
        inOrder.verify(printStream).println("INFO: ".concat(msg));
        inOrder.verify(printStream).println("DEBUG: ".concat(msgAfter1));
        inOrder.verify(printStream).println(separator);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void test() {
        PrintStream printStream = mock(PrintStream.class);
        Thread thread = new Thread(() -> Assertions.fail("should not be called"), random());
        String msg = random();
        String name = random();
        Throwable throwable = new RuntimeException(random());
        PrinterStreamAppender fixture = new PrinterStreamAppender(printStream);

        assertThrows(RuntimeException.class, () -> fixture.write(null));
        fixture.setPattern("$thread $name: $msg$exception(showStackTrace:false)");

        assertThrows(RuntimeException.class, () -> fixture.write(null));
        fixture.setLevel(LogLevel.DEBUG);

        assertDoesNotThrow(() -> fixture.write(new Event(thread, LogLevel.TRACE, name, msg, throwable)));
        fixture.write(new Event(thread, LogLevel.INFO, name, msg, throwable));

        verify(printStream).println(String.format("%s %s: %s%n%s: %s", thread.getName(), name, msg, throwable.getClass().getName(), throwable.getMessage()));

        verifyNoMoreInteractions(printStream);
    }

}