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

public record SimpleLogger(String name, LoggingDirector loggingDirector) implements Logger {

    @Override
    public boolean has(final LogLevel level) {
        return getLevel().getWeight() <= level.getWeight();
    }

    @Override
    public void holdAll() {
        loggingDirector.hold();
    }

    @Override
    public void freezeAll() {
        loggingDirector.freeze();
    }

    @Override
    public boolean allOnHold() {
        return loggingDirector.onHold();
    }

    @Override
    public void resumeAll() {
        loggingDirector.resume();
    }

    @Override
    public LogLevel getLevel() {
        return loggingDirector.getLowestLevel(name);
    }

    @Override
    public void fatal(final String msg) {
        log(LogLevel.FATAL, msg);
    }

    @Override
    public void fatal(final String msg, final Throwable e) {
        log(LogLevel.FATAL, msg, e);
    }

    @Override
    public void fatal(final Throwable e) {
        log(LogLevel.FATAL, e);
    }

    @Override
    public void error(final String msg) {
        log(LogLevel.ERROR, msg);
    }

    @Override
    public void error(final String msg, final Throwable e) {
        log(LogLevel.ERROR, msg, e);
    }

    @Override
    public void error(final Throwable e) {
        log(LogLevel.ERROR, e);
    }

    @Override
    public void warn(final String msg) {
        log(LogLevel.WARN, msg);
    }

    @Override
    public void warn(final String msg, final Throwable e) {
        log(LogLevel.WARN, msg, e);
    }

    @Override
    public void warn(final Throwable e) {
        log(LogLevel.WARN, e);
    }

    @Override
    public void info(final String msg) {
        log(LogLevel.INFO, msg);
    }

    @Override
    public void info(final String msg, final Throwable e) {
        log(LogLevel.INFO, msg, e);
    }

    @Override
    public void info(final Throwable e) {
        log(LogLevel.INFO, e);
    }

    @Override
    public void debug(final String msg) {
        log(LogLevel.DEBUG, msg);
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        log(LogLevel.DEBUG, msg, e);
    }

    @Override
    public void debug(final Throwable e) {
        log(LogLevel.DEBUG, e);
    }

    @Override
    public void trace(final String msg) {
        log(LogLevel.TRACE, msg);
    }

    public void log(final LogLevel level, final String msg) {
        if (level != LogLevel.OFF) {
            loggingDirector.push(level, name, msg, null);
        }
    }

    public void log(final LogLevel level, final Throwable e) {
        log(level, null, e);
    }

    public void log(final LogLevel level, final String msg, final Throwable e) {
        if (level != LogLevel.OFF) {
            loggingDirector.push(level, name, msg, e);
        }
    }
}