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

import com.webxells.dis.api.Logger;

public class NullLogger implements StartUpLogger {

    @Override
    public boolean has(final LogLevel level) {
        return false;
    }

    @Override
    public void holdAll() { }

    @Override
    public void freezeAll() { }

    @Override
    public boolean allOnHold() {
        return false;
    }

    @Override
    public void resumeAll() { }

    @Override
    public LogLevel getLevel() {
        return LogLevel.TRACE;
    }

    @Override
    public void fatal(final String s) { }

    @Override
    public void fatal(final String s, final Throwable throwable) { }

    @Override
    public void fatal(final Throwable throwable) { }

    @Override
    public void error(final String s) { }

    @Override
    public void error(final String s, final Throwable throwable) { }

    @Override
    public void error(final Throwable throwable) { }

    @Override
    public void warn(final String s) { }

    @Override
    public void warn(final String msg, final Throwable e) { }

    @Override
    public void warn(final Throwable e) { }

    @Override
    public void info(final String s) { }

    @Override
    public void info(final String msg, final Throwable e) { }

    @Override
    public void info(final Throwable e) { }

    @Override
    public void debug(final String s) { }

    @Override
    public void debug(final String msg, final Throwable e) { }

    @Override
    public void debug(final Throwable e) { }

    @Override
    public void trace(final String s) { }

    @Override
    public void handleStartUp(final Logger logger) { }
}