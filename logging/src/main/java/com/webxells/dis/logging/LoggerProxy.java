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

public class LoggerProxy implements Logger {
    private Logger logger;
    private String loggingStringSubject;
    private Class<?> loggingClazzSubject;

    LoggerProxy(final String name) {
        loggingStringSubject = name;
    }

    LoggerProxy(final Class<?> clazz) {
        loggingClazzSubject = clazz;
    }

    @Override
    public boolean has(final LogLevel level) {
        return getLogger().has(level);
    }

    @Override
    public void holdAll() {
        getLogger().holdAll();
    }

    @Override
    public void freezeAll() {
        getLogger().freezeAll();
    }

    @Override
    public boolean allOnHold() {
        return getLogger().allOnHold();
    }

    @Override
    public void resumeAll() {
        getLogger().resumeAll();
    }

    @Override
    public LogLevel getLevel() {
        return getLogger().getLevel();
    }

    @Override
    public void fatal(final String msg) {
        getLogger().fatal(msg);
    }

    @Override
    public void fatal(final String msg, final Throwable e) {
        getLogger().fatal(msg, e);
    }

    @Override
    public void fatal(final Throwable e) {
        getLogger().fatal(e);
    }

    @Override
    public void error(final String msg) {
        getLogger().error(msg);
    }

    @Override
    public void error(final String msg, final Throwable e) {
        getLogger().error(msg, e);
    }

    @Override
    public void error(final Throwable e) {
        getLogger().error(e);
    }

    @Override
    public void warn(final String msg) {
        getLogger().warn(msg);
    }

    @Override
    public void warn(final String msg, final Throwable e) {
        getLogger().warn(msg, e);
    }

    @Override
    public void warn(final Throwable e) {
        getLogger().warn(e);
    }

    @Override
    public void info(final String msg) {
        getLogger().info(msg);
    }

    @Override
    public void info(final String msg, final Throwable e) {
        getLogger().info(msg, e);
    }

    @Override
    public void info(final Throwable e) {
        getLogger().info(e);
    }

    @Override
    public void debug(final String msg) {
        getLogger().debug(msg);
    }

    @Override
    public void debug(final String msg, final Throwable e) {
        getLogger().debug(msg, e);
    }

    @Override
    public void debug(final Throwable e) {
        getLogger().debug(e);
    }

    @Override
    public void trace(final String msg) {
        getLogger().trace(msg);
    }

    private Logger getLogger() {
        refreshLogger();
        return null == logger ? LoggerProxyFactory.getStartUpLogger() : logger;
    }

    private void refreshLogger() {
        if (null == logger && LoggerProxyFactory.logManagerRegistered()) {
            if (null != loggingClazzSubject) {
                logger = LoggerProxyFactory.logManager().get(loggingClazzSubject);
            } else if (null != loggingStringSubject) {
                logger = LoggerProxyFactory.logManager().get(loggingStringSubject);
            } else {
                throw new RuntimeException("Invalid loggingSubject");
            }

        }
    }
}