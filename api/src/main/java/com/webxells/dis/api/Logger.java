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
package com.webxells.dis.api;

import java.util.function.Supplier;

public interface Logger {
    enum LogLevel {
        OFF(6), FATAL(5), ERROR(4), WARN(3), INFO(2), DEBUG(1), TRACE(0);

        private final int weight;

        LogLevel(final int weight) {
            this.weight = weight;
        }

        public int getWeight() {
            return weight;
        }
    }

    boolean has(LogLevel level);

    void holdAll();
    void freezeAll();
    boolean allOnHold();
    void resumeAll();

    /* @todo: maybe later
    void hold();
    void freeze();
    boolean onHold();
    void resume();
     */

    LogLevel getLevel();

    default void logIfFatal(Supplier<String> createLogMessage) {
        if (has(LogLevel.FATAL)) {
            f(createLogMessage.get());
        }
    }

    void fatal(String msg);

    default void fatal(final String msg, final Object... formatArgs) {
        fatal(String.format(msg, formatArgs));
    }

    void fatal(String msg, Throwable e);

    default void fatal(final String msg, final Throwable e, final Object... formatArgs) {
        fatal(String.format(msg, formatArgs), e);
    }

    void fatal(Throwable e);

    default void f(final Throwable e) {
        fatal(e);
    }

    default void f(final String msg, final Throwable e) {
        fatal(msg, e);
    }

    default void f(final String msg, final Throwable e, final Object... formatArgs) {
        fatal(msg, e, formatArgs);
    }

    default void f(final String msg) {
        fatal(msg);
    }

    default void f(final String msg, final Object... formatArgs) {
        fatal(msg, formatArgs);
    }

    default void logIfError(Supplier<String> createLogMessage) {
        if (has(LogLevel.ERROR)) {
            e(createLogMessage.get());
        }
    }

    void error(String msg);

    default void error(final String msg, final Object... formatArgs) {
        error(String.format(msg, formatArgs));
    }

    void error(String msg, Throwable e);

    default void error(final String msg, final Throwable e, final Object... formatArgs) {
        error(String.format(msg, formatArgs), e);
    }

    void error(Throwable e);

    default void e(final String msg, final Throwable e) {
        error(msg, e);
    }

    default void e(final String msg, final Throwable e, final Object... formatArgs) {
        error(msg, e, formatArgs);
    }

    default void e(final String msg) {
        error(msg);
    }

    default void e(final String msg, final Object... formatArgs) {
        error(msg, formatArgs);
    }

    default void e(final Throwable e) {
        error(e);
    }

    default void logIfWarn(Supplier<String> createLogMessage) {
        if (has(LogLevel.WARN)) {
            w(createLogMessage.get());
        }
    }

    void warn(String msg);

    default void warn(final String msg, final Object... formatArgs) {
        warn(String.format(msg, formatArgs));
    }

    void warn(String msg, Throwable e);

    default void warn(final String msg, final Throwable e, final Object... formatArgs) {
        warn(String.format(msg, formatArgs), e);
    }

    void warn(Throwable e);

    default void w(final String msg) {
        warn(msg);
    }

    default void w(final String msg, final Throwable e) {
        warn(msg, e);
    }

    default void w(final String msg, final Throwable e, final Object... formatArgs) {
        warn(msg, e, formatArgs);
    }

    default void w(final String msg, final Object... formatArgs) {
        warn(msg, formatArgs);
    }

    default void w(final Throwable e) {
        warn(e);
    }

    default void logIfInfo(Supplier<String> createLogMessage) {
        if (has(LogLevel.INFO)) {
            i(createLogMessage.get());
        }
    }

    void info(String msg);

    default void info(final String msg, final Object... formatArgs) {
        info(String.format(msg, formatArgs));
    }

    void info(final String msg, final Throwable e);

    default void info(final String msg, final Throwable e, final Object... formatArgs) {
        info(String.format(msg, formatArgs), e);
    }

    void info(Throwable e);

    default void i(final String msg) {
        info(msg);
    }

    default void i(final String msg, final Object... formatArgs) {
        info(msg, formatArgs);
    }

    default void i(final String msg, final Throwable e) {
        info(msg, e);
    }

    default void i(final String msg, final Throwable e, final Object... formatArgs) {
        info(msg, e, formatArgs);
    }

    default void i(final Throwable e) {
        info(e);
    }

    default void logIfDebug(Supplier<String> createLogMessage) {
        if (has(LogLevel.DEBUG)) {
            d(createLogMessage.get());
        }
    }

    void debug(String msg);

    default void debug(final String msg, final Object... formatArgs) {
        debug(String.format(msg, formatArgs));
    }

    void debug(String msg, Throwable e);

    default void debug(final String msg, final Throwable e, final Object... formatArgs) {
        debug(String.format(msg, formatArgs), e);
    }

    void debug(Throwable e);

    default void d(final String msg) {
        debug(msg);
    }

    default void d(final String msg, final Object... formatArgs) {
        debug(msg, formatArgs);
    }

    default void d(final String msg, final Throwable e) {
        debug(msg, e);
    }

    default void d(final String msg, final Throwable e, final Object... formatArgs) {
        debug(msg, e, formatArgs);
    }

    default void d(final Throwable e) {
        debug(e);
    }

    default void logIfTrace(Supplier<String> createLogMessage) {
        if (has(LogLevel.TRACE)) {
            t(createLogMessage.get());
        }
    }

    void trace(String msg);

    default void trace(final String msg, final Object... formatArgs) {
        trace(String.format(msg, formatArgs));
    }

    default void t(final String msg) {
        trace(msg);
    }

    default void t(final String msg, final Object... formatArgs) {
        trace(msg, formatArgs);
    }

    default void logIf(LogLevel level, Supplier<String> createLogMessage) {
        if (has(level)) {
            log(level, createLogMessage.get());
        }
    }


    default void log(final LogLevel level, final String msg, final Throwable e) {
        switch (level) {
            case FATAL -> f(msg, e);
            case ERROR -> e(msg, e);
            case WARN -> w(msg, e);
            case INFO -> i(msg, e);
            case DEBUG -> d(msg, e);
            case TRACE -> t(msg, e);
        }
    }

    default void log(final LogLevel level, final String msg) {
        switch (level) {
            case FATAL -> f(msg);
            case ERROR -> e(msg);
            case WARN -> w(msg);
            case INFO -> i(msg);
            case DEBUG -> d(msg);
            case TRACE -> t(msg);
        }
    }

    default void log(final LogLevel level,final  String msg, final Throwable e, final Object... formatArgs) {
        switch (level) {
            case FATAL -> f(msg, e, formatArgs);
            case ERROR -> e(msg, e, formatArgs);
            case WARN -> w(msg, e, formatArgs);
            case INFO -> i(msg, e, formatArgs);
            case DEBUG -> d(msg, e, formatArgs);
            case TRACE -> t(msg, e, formatArgs);
        }
    }

    default void log(final LogLevel level, final String msg, final Object... formatArgs) {
        switch (level) {
            case FATAL -> f(msg, formatArgs);
            case ERROR -> e(msg, formatArgs);
            case WARN -> w(msg, formatArgs);
            case INFO -> i(msg, formatArgs);
            case DEBUG -> d(msg, formatArgs);
            case TRACE -> t(msg, formatArgs);
        }
    }
}