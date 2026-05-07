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
package com.webxells.dis.logging.simple.internal;

import com.webxells.dis.api.Logger;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

class SimpleLoggerTest {

    @Test
    void test() {
        Exception exception = new RuntimeException();
        ConcurrentLoggingDirector director = Mockito.mock(ConcurrentLoggingDirector.class);
        Mockito.when(director.getLowestLevel(any())).thenReturn(Logger.LogLevel.INFO);
        Mockito.when(director.onHold()).thenReturn(false);

        SimpleLogger fixture = new SimpleLogger("test", director);
        assertEquals(Logger.LogLevel.INFO, fixture.getLevel());
        fixture.fatal("1");
        fixture.fatal(exception);
        fixture.fatal("2", exception);
        fixture.fatal("%s", "3");
        fixture.fatal("%s", exception, "4");


        fixture.error("5");
        fixture.error(exception);
        fixture.error("6", exception);
        fixture.error("%s", "7");
        fixture.error("%s", exception, "8");

        fixture.warn("9");
        fixture.warn(exception);
        fixture.warn("10", exception);
        fixture.warn("%s", "11");
        fixture.warn("%s", exception, "12");

        fixture.info("13");
        fixture.info(exception);
        fixture.info("14", exception);
        fixture.info("%s", "15");
        fixture.info("%s", exception, "16");

        fixture.debug("17");
        fixture.debug(exception);
        fixture.debug("18", exception);
        fixture.debug("%s", "19");
        fixture.debug("%s", exception, "20");

        fixture.trace("21");
        fixture.trace("%s", "22");

        assertFalse(fixture.allOnHold());
        fixture.holdAll();
        fixture.freezeAll();
        fixture.resumeAll();

        verify(director).getLowestLevel(any());

        verify(director).push(Logger.LogLevel.FATAL, "test", "1", null);
        verify(director).push(Logger.LogLevel.FATAL, "test", null, exception);
        verify(director).push(Logger.LogLevel.FATAL, "test", "2", exception);
        verify(director).push(Logger.LogLevel.FATAL, "test", "3", null);
        verify(director).push(Logger.LogLevel.FATAL, "test", "4", exception);

        verify(director).push(Logger.LogLevel.ERROR, "test", "5", null);
        verify(director).push(Logger.LogLevel.ERROR, "test", null, exception);
        verify(director).push(Logger.LogLevel.ERROR, "test", "6", exception);
        verify(director).push(Logger.LogLevel.ERROR, "test", "7", null);
        verify(director).push(Logger.LogLevel.ERROR, "test", "8", exception);

        verify(director).push(Logger.LogLevel.WARN, "test", "9", null);
        verify(director).push(Logger.LogLevel.WARN, "test", null, exception);
        verify(director).push(Logger.LogLevel.WARN, "test", "10", exception);
        verify(director).push(Logger.LogLevel.WARN, "test", "11", null);
        verify(director).push(Logger.LogLevel.WARN, "test", "12", exception);

        verify(director).push(Logger.LogLevel.INFO, "test", "13", null);
        verify(director).push(Logger.LogLevel.INFO, "test", null, exception);
        verify(director).push(Logger.LogLevel.INFO, "test", "14", exception);
        verify(director).push(Logger.LogLevel.INFO, "test", "15", null);
        verify(director).push(Logger.LogLevel.INFO, "test", "16", exception);

        verify(director).push(Logger.LogLevel.DEBUG, "test", "17", null);
        verify(director).push(Logger.LogLevel.DEBUG, "test", null, exception);
        verify(director).push(Logger.LogLevel.DEBUG, "test", "18", exception);
        verify(director).push(Logger.LogLevel.DEBUG, "test", "19", null);
        verify(director).push(Logger.LogLevel.DEBUG, "test", "20", exception);

        verify(director).push(Logger.LogLevel.TRACE, "test", "21", null);
        verify(director).push(Logger.LogLevel.TRACE, "test", "22", null);
        verify(director).onHold();
        verify(director).hold();
        verify(director).freeze();
        verify(director).resume();

        verifyNoMoreInteractions(director);
    }

}