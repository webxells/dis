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
package org.slf4j;

import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.event.Level;

public interface Logger {
    String ROOT_LOGGER_NAME = "ROOT";

    String getName();

    LoggingEventBuilder makeLoggingEventBuilder(Level level);

    LoggingEventBuilder atLevel(Level level);

    boolean isEnabledForLevel(Level level);

    boolean isTraceEnabled();

    void trace(String var1);

    void trace(String var1, Object var2);

    void trace(String var1, Object var2, Object var3);

    void trace(String var1, Object... var2);

    void trace(String var1, Throwable var2);

    boolean isTraceEnabled(Marker var1);

    LoggingEventBuilder atTrace();

    void trace(Marker var1, String var2);

    void trace(Marker var1, String var2, Object var3);

    void trace(Marker var1, String var2, Object var3, Object var4);

    void trace(Marker var1, String var2, Object... var3);

    void trace(Marker var1, String var2, Throwable var3);

    boolean isDebugEnabled();

    void debug(String var1);

    void debug(String var1, Object var2);

    void debug(String var1, Object var2, Object var3);

    void debug(String var1, Object... var2);

    void debug(String var1, Throwable var2);

    boolean isDebugEnabled(Marker var1);

    void debug(Marker var1, String var2);

    void debug(Marker var1, String var2, Object var3);

    void debug(Marker var1, String var2, Object var3, Object var4);

    void debug(Marker var1, String var2, Object... var3);

    void debug(Marker var1, String var2, Throwable var3);

    LoggingEventBuilder atDebug();

    boolean isInfoEnabled();

    void info(String var1);

    void info(String var1, Object var2);

    void info(String var1, Object var2, Object var3);

    void info(String var1, Object... var2);

    void info(String var1, Throwable var2);

    boolean isInfoEnabled(Marker var1);

    void info(Marker var1, String var2);

    void info(Marker var1, String var2, Object var3);

    void info(Marker var1, String var2, Object var3, Object var4);

    void info(Marker var1, String var2, Object... var3);

    void info(Marker var1, String var2, Throwable var3);

    LoggingEventBuilder atInfo();

    boolean isWarnEnabled();

    void warn(String var1);

    void warn(String var1, Object var2);

    void warn(String var1, Object... var2);

    void warn(String var1, Object var2, Object var3);

    void warn(String var1, Throwable var2);

    boolean isWarnEnabled(Marker var1);

    void warn(Marker var1, String var2);

    void warn(Marker var1, String var2, Object var3);

    void warn(Marker var1, String var2, Object var3, Object var4);

    void warn(Marker var1, String var2, Object... var3);

    void warn(Marker var1, String var2, Throwable var3);

    LoggingEventBuilder atWarn();

    boolean isErrorEnabled();

    void error(String var1);

    void error(String var1, Object var2);

    void error(String var1, Object var2, Object var3);

    void error(String var1, Object... var2);

    void error(String var1, Throwable var2);

    boolean isErrorEnabled(Marker var1);

    void error(Marker var1, String var2);

    void error(Marker var1, String var2, Object var3);

    void error(Marker var1, String var2, Object var3, Object var4);

    void error(Marker var1, String var2, Object... var3);

    void error(Marker var1, String var2, Throwable var3);

    LoggingEventBuilder atError();
}