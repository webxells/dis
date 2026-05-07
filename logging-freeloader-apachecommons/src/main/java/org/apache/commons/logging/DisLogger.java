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
package org.apache.commons.logging;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.logging.LoggerProxyFactory;

public class DisLogger implements Log {
    private final Logger logger;

    private static String getMessage(final Object message) {
        if (null == message) {
            return "null";
        }
        if (message instanceof String asString) {
            return asString;
        }
        return String.valueOf(message);
    }

    private static String getClassName(final Object o) {
        if (o instanceof Class<?> asClass) {
            return asClass.getName();
        }
        return getMessage(o);
    }

    public DisLogger(final Object o) {
        this(getClassName(o));
    }

    public DisLogger(final String name) {
        logger = LoggerProxyFactory.logger("C.".concat(name));
    }

    @Override
    public void fatal(final Object message) {
        logger.f(getMessage(message));
    }

    @Override
    public void fatal(final Object message, final Throwable t) {
        logger.f(getMessage(message), t);
    }

    @Override
    public void error(final Object message) {
        logger.e(getMessage(message));
    }

    @Override
    public void error(final Object message, final Throwable t) {
        logger.e(getMessage(message), t);
    }

    @Override
    public void warn(final Object message) {
        logger.w(getMessage(message));
    }

    @Override
    public void warn(final Object message, final Throwable t) {
        logger.w(getMessage(message), t);
    }

    @Override
    public void info(final Object message) {
        logger.i(getMessage(message));
    }

    @Override
    public void info(final Object message, final Throwable t) {
        logger.i(getMessage(message), t);
    }

    @Override
    public void debug(final Object message) {
        logger.d(getMessage(message));
    }

    @Override
    public void debug(final Object message, final Throwable t) {
        logger.d(getMessage(message), t);
    }

    @Override
    public void trace(final Object message) {
        logger.t(getMessage(message));
    }

    @Override
    public void trace(final Object message, final Throwable t) {
        debug(getMessage(message), t);
    }

    @Override
    public boolean isFatalEnabled() {
        return isEnabled(LogLevel.FATAL);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(LogLevel.ERROR);
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(LogLevel.INFO);
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(LogLevel.WARN);
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(LogLevel.DEBUG);
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(LogLevel.TRACE);
    }

    private boolean isEnabled(final LogLevel logLevel) {
        return logLevel.getWeight() <= logger.getLevel().getWeight();
    }
}