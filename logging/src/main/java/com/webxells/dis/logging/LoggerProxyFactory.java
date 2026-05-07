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
import java.util.Objects;

public class LoggerProxyFactory {
    private static final StartUpLogger startUpLogger = createStartUpLogger();

    private static LogManager logManager;

    public static Logger logger(final Class<?> clazz) {
        return new LoggerProxy(clazz);
    }

    public static Logger logger(final String name) {
        return new LoggerProxy(name);
    }

    public static void registerLogManager(final LogManager logManager) {
        LoggerProxyFactory.logManager = logManager;
        startUpLogger.handleStartUp(logManager.get("StartUp"));
    }

    static boolean logManagerRegistered() {
        return Objects.nonNull(logManager);
    }

    static LogManager logManager() {
        return logManager;
    }

    static StartUpLogger getStartUpLogger() {
        return startUpLogger;
    }

    static StartUpLogger createStartUpLogger() {
        if (null == System.getProperty("without-start-logging")) {
            return new RememberStartUpErrorsLogger();
        }
        return new NullLogger();
    }
}