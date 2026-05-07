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

import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Optional;
import org.slf4j.event.Level;
import org.slf4j.spi.LocationAwareLogger;
import org.slf4j.spi.LoggingEventBuilder;
import org.slf4j.spi.SimpleLoggingEventBuilder;

public class DisLogger4Slf4j implements LocationAwareLogger {
    private final String name;
    private final com.webxells.dis.api.Logger logger;

    public DisLogger4Slf4j(String name) {
        this.name = name;
        logger = LoggerProxyFactory.logger("F.".concat(name));
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public LoggingEventBuilder makeLoggingEventBuilder(final Level level) {
        return atLevel(level);
    }

    @Override
    public LoggingEventBuilder atLevel(final Level level) {
        return new SimpleLoggingEventBuilder(level, this);
    }

    @Override
    public LoggingEventBuilder atTrace() {
        return atLevel(Level.TRACE);
    }

    @Override
    public LoggingEventBuilder atDebug() {
        return atLevel(Level.DEBUG);
    }

    @Override
    public LoggingEventBuilder atInfo() {
        return atLevel(Level.INFO);
    }

    @Override
    public LoggingEventBuilder atWarn() {
        return atLevel(Level.WARN);
    }

    @Override
    public LoggingEventBuilder atError() {
        return atLevel(Level.ERROR);
    }

    @Override
    public boolean isEnabledForLevel(final Level level) {
        final int currentWeight = Math.min(logger.getLevel().getWeight() * 10, Level.ERROR.toInt());
        return currentWeight <= level.toInt();
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabledForLevel(Level.TRACE);
    }

    @Override
    public void trace(final String message) {
        logger.t(message);
    }

    @Override
    public void trace(final String format, final Object obj) {
        trace(String.format(format, obj));
    }

    @Override
    public void trace(final String format, final Object obj1, final Object obj2) {
        trace(String.format(format, obj1, obj2));
    }

    @Override
    public void trace(final String format, final Object... objects) {
        trace(String.format(format, objects));
    }

    @Override
    public void trace(final String message, final Throwable throwable) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.t(message, a), () -> trace(message));
    }

    @Override
    public boolean isTraceEnabled(final Marker var1) {
        return isEnabledForLevel(Level.TRACE);
    }

    @Override
    public void trace(final Marker var1, final String var2) {
        trace(var2);
    }

    @Override
    public void trace(final Marker var1, final String var2, final Object var3) {
        trace(var2, var3);
    }

    @Override
    public void trace(final Marker var1, final String var2, final Object var3, final Object var4) {
        trace(var2, var3, var4);
    }

    @Override
    public void trace(final Marker var1, final String var2, final Object... var3) {
        trace(var2, var3);
    }

    @Override
    public void trace(final Marker var1, final String var2, final Throwable var3) {
        trace(var2, var3);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabledForLevel(Level.DEBUG);
    }

    @Override
    public void debug(final String message) {
        logger.d(message);
    }

    @Override
    public void debug(final String format, final Object obj) {
        debug(String.format(format, obj));
    }

    @Override
    public void debug(final String format, final Object obj1, final Object obj2) {
        debug(String.format(format, obj1, obj2));
    }

    @Override
    public void debug(final String format, final Object... objects) {
        debug(String.format(format, objects));
    }

    @Override
    public void debug(final String message, final Throwable throwable) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.d(message, a), () -> debug(message));
    }

    @Override
    public boolean isDebugEnabled(final Marker var1) {
        return isEnabledForLevel(Level.DEBUG);
    }

    @Override
    public void debug(final Marker var1, final String var2) {
        debug(var2);
    }

    @Override
    public void debug(final Marker var1, final String var2, final Object var3) {
        debug(var2, var3);
    }

    @Override
    public void debug(final Marker var1, final String var2, final Object var3, final Object var4) {
        debug(var2, var3, var4);
    }

    @Override
    public void debug(final Marker var1, final String var2, final Object... var3) {
        debug(var2, var3);
    }

    @Override
    public void debug(final Marker var1, final String var2, final Throwable var3) {
        debug(var2, var3);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabledForLevel(Level.INFO);
    }

    @Override
    public void info(final String message) {
        logger.i(message);
    }

    @Override
    public void info(final String format, final Object obj) {
        info(String.format(format, obj));
    }

    @Override
    public void info(final String format, final Object obj1, final Object obj2) {
        info(String.format(format, obj1, obj2));
    }

    @Override
    public void info(final String format, final Object... objects) {
        info(String.format(format, objects));
    }

    @Override
    public void info(final String message, final Throwable throwable) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.i(message, a), () -> info(message));
    }

    @Override
    public boolean isInfoEnabled(final Marker var1) {
        return isEnabledForLevel(Level.INFO);
    }

    @Override
    public void info(final Marker var1, final String var2) {
        info(var2);
    }

    @Override
    public void info(final Marker var1, final String var2, final Object var3) {
        info(var2, var3);
    }

    @Override
    public void info(final Marker var1, final String var2, final Object var3, final Object var4) {
        info(var2, var3, var4);
    }

    @Override
    public void info(final Marker var1, final String var2, final Object... var3) {
        info(var2, var3);
    }

    @Override
    public void info(final Marker var1, final String var2, final Throwable var3) {
        info(var2, var3);
    }


    @Override
    public boolean isWarnEnabled() {
        return isEnabledForLevel(Level.WARN);
    }

    @Override
    public void warn(final String message) {
        logger.w(message);
    }

    @Override
    public void warn(final String format, final Object obj) {
        warn(String.format(format, obj));
    }

    @Override
    public void warn(final String format, final Object obj1, final Object obj2) {
        warn(String.format(format, obj1, obj2));
    }

    @Override
    public void warn(final String format, final Object... objects) {
        warn(String.format(format, objects));
    }

    @Override
    public void warn(final String message, final Throwable throwable) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.w(message, a), () -> warn(message));
    }

    @Override
    public boolean isWarnEnabled(final Marker var1) {
        return isEnabledForLevel(Level.WARN);
    }

    @Override
    public void warn(final Marker var1, final String var2) {
        warn(var2);
    }

    @Override
    public void warn(final Marker var1, final String var2, final Object var3) {
        warn(var2, var3);
    }

    @Override
    public void warn(final Marker var1, final String var2, final Object var3, final Object var4) {
        warn(var2, var3, var4);
    }

    @Override
    public void warn(final Marker var1, final String var2, final Object... var3) {
        warn(var2, var3);
    }

    @Override
    public void warn(final Marker var1, final String var2, final Throwable var3) {
        warn(var2, var3);
    }


    @Override
    public boolean isErrorEnabled() {
        return isEnabledForLevel(Level.ERROR);
    }

    @Override
    public void error(final String message) {
        logger.e(message);
    }

    @Override
    public void error(final String format, final Object obj) {
        error(String.format(format, obj));
    }

    @Override
    public void error(final String format, final Object obj1, final Object obj2) {
        error(String.format(format, obj1, obj2));
    }

    @Override
    public void error(final String format, final Object... objects) {
        error(String.format(format, objects));
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.e(message, a), () -> error(message));
    }

    @Override
    public boolean isErrorEnabled(final Marker var1) {
        return isEnabledForLevel(Level.ERROR);
    }

    @Override
    public void error(final Marker var1, final String var2) {
        error(var2);
    }

    @Override
    public void error(final Marker var1, final String var2, final Object var3) {
        error(var2, var3);
    }

    @Override
    public void error(final Marker var1, final String var2, final Object var3, final Object var4) {
        error(var2, var3, var4);
    }

    @Override
    public void error(final Marker var1, final String var2, final Object... var3) {
        error(var2, var3);
    }

    @Override
    public void error(final Marker var1, final String var2, final Throwable var3) {
        error(var2, var3);
    }

    @Override
    public void log(final Marker marker, final String fqcn, final int level, final String message, final Object[] argArray, final Throwable t) {
        switch (level) {
            case LocationAwareLogger.TRACE_INT -> trace(message, t);
            case LocationAwareLogger.DEBUG_INT -> debug(message, t);
            case LocationAwareLogger.INFO_INT -> info(message, t);
            case LocationAwareLogger.WARN_INT -> warn(message, t);
            case LocationAwareLogger.ERROR_INT -> error(message, t);
        }
    }
}