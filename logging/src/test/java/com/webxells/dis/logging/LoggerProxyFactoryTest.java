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
package com.webxells.dis.logging;

import com.webxells.dis.api.LogManager;
import com.webxells.dis.api.Logger;
import com.webxells.dis.test.cases.SimpleTestCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

class LoggerProxyFactoryTest {

    @Test
    void testRememberingLogger() {
        System.clearProperty("without-start-logging");

        String loggerName = random("loggerName");
        String msg1 = random("msg 1");
        String msg2 = random("msg 2");
        String msg3 = random("msg 3");
        Throwable throwable = mock(Throwable.class);

        Logger testLogger = LoggerProxyFactory.logger(loggerName);
        assertInstanceOf(LoggerProxy.class, testLogger);
        testLogger.fatal(msg1);
        testLogger.fatal(msg2, throwable);
        testLogger.fatal(throwable);
        testLogger.debug(msg3);
        testLogger.debug(msg3, throwable);
        testLogger.debug(throwable);
        testLogger.warn(msg3);
        testLogger.warn(msg3, throwable);
        testLogger.warn(throwable);
        testLogger.info(msg2);
        testLogger.info(msg2, throwable);
        testLogger.info(throwable);
        testLogger.error(msg2);
        testLogger.error(msg2, throwable);
        testLogger.error(throwable);
        testLogger.trace(msg2);
        testLogger.holdAll();
        testLogger.freezeAll();
        assertTrue(testLogger.allOnHold());
        LogManager logManager = mock(LogManager.class);
        Logger logger = mock(Logger.class);
        Logger startUpLogger = mock(Logger.class);
        when(logManager.get("StartUp")).thenReturn(startUpLogger);
        when(logManager.get(loggerName)).thenReturn(logger);
        LoggerProxyFactory.registerLogManager(logManager);

        verify(startUpLogger).fatal(msg1);
        verify(startUpLogger).fatal(msg2, throwable);
        verify(startUpLogger).fatal(throwable);
        verify(startUpLogger).debug(msg3);
        verify(startUpLogger).debug(msg3, throwable);
        verify(startUpLogger).debug(throwable);
        verify(startUpLogger).warn(msg3);
        verify(startUpLogger).warn(msg3, throwable);
        verify(startUpLogger).warn(throwable);
        verify(startUpLogger).info(msg2);
        verify(startUpLogger).info(msg2, throwable);
        verify(startUpLogger).info(throwable);
        verify(startUpLogger).error(msg2);
        verify(startUpLogger).error(msg2, throwable);
        verify(startUpLogger).error(throwable);
        verify(startUpLogger).trace(msg2);
        verify(startUpLogger).freezeAll();

        testLogger.fatal(msg2);
        verify(logManager).get("StartUp");
        verify(logManager).get(loggerName);
        verify(logger).fatal(msg2);
        testLogger.debug(msg3);
        testLogger.debug(msg3, throwable);
        verify(logger).debug(msg3);
        verify(logger).debug(msg3, throwable);
        testLogger.warn(msg3);
        testLogger.warn(msg3, throwable);
        verify(logger).warn(msg3);
        verify(logger).warn(msg3, throwable);
        testLogger.info(msg2);
        testLogger.info(msg2, throwable);
        verify(logger).info(msg2);
        verify(logger).info(msg2, throwable);
        testLogger.error(msg2);
        testLogger.error(msg2, throwable);
        verify(logger).error(msg2);
        verify(logger).error(msg2, throwable);
        testLogger.trace(msg2);
        verify(logger).trace(msg2);
        testLogger.resumeAll();
        verify(logger).resumeAll();
        verifyNoMoreInteractions(logManager, logger, startUpLogger);
    }

    @Test
    void testNullLogger() {
        System.setProperty("without-start-logging", "");

        String msg1 = random("msg 1");
        String msg2 = random("msg 2");
        String msg3 = random("msg 3");
        Throwable throwable = mock(Throwable.class);

        StartUpLogger startLogger = LoggerProxyFactory.createStartUpLogger();
        assertInstanceOf(NullLogger.class, startLogger);
        startLogger.fatal(msg1);
        startLogger.fatal(msg2, throwable);
        startLogger.fatal(throwable);
        startLogger.debug(msg3);
        startLogger.debug(msg3, throwable);
        startLogger.debug(throwable);
        startLogger.warn(msg3);
        startLogger.warn(msg3, throwable);
        startLogger.warn(throwable);
        startLogger.info(msg2);
        startLogger.info(msg2, throwable);
        startLogger.info(throwable);
        startLogger.error(msg2);
        startLogger.error(msg2, throwable);
        startLogger.error(throwable);
        startLogger.trace(msg2);
        startLogger.holdAll();
        startLogger.freezeAll();
        assertFalse(startLogger.allOnHold());
        Logger startUpLogger = mock(Logger.class);

        startLogger.handleStartUp(startUpLogger);

        verifyNoMoreInteractions(startUpLogger);
    }

    private String random(final String string) {
        return SimpleTestCase.random(string);
    }



}