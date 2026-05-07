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
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RememberStartUpErrorsLogger implements StartUpLogger {
    public record LogEvent(LogLevel level, String msg, Throwable e) { }

    private final Queue<LogEvent> memory = new ConcurrentLinkedQueue<>();
    private Boolean startNormally = true;

    @Override
    public boolean has(final LogLevel level) {
        return true;
    }

    @Override
    public void holdAll() {
        startNormally = false;
    }

    @Override
    public void freezeAll() {
        startNormally = null;
    }

    @Override
    public boolean allOnHold() {
        return null == startNormally || !startNormally;
    }

    @Override
    public void resumeAll() {
        startNormally = true;
    }

    @Override
    public void handleStartUp(final Logger logger) {
        memory.forEach(event -> {
            switch(event.level) {
                case FATAL -> {
                    if (null == event.e) {
                        logger.fatal(event.msg);
                    } else if(null == event.msg) {
                        logger.fatal(event.e);
                    } else {
                        logger.fatal(event.msg, event.e);
                    }
                }
                case ERROR -> {
                    if (null == event.e) {
                        logger.error(event.msg);
                    } else if(null == event.msg) {
                        logger.error(event.e);
                    } else {
                        logger.error(event.msg, event.e);
                    }
                }
                case WARN -> {
                    if (null == event.e) {
                        logger.warn(event.msg);
                    } else if(null == event.msg) {
                        logger.warn(event.e);
                    } else {
                        logger.warn(event.msg, event.e);
                    }
                }
                case INFO -> {
                    if (null == event.e) {
                        logger.info(event.msg);
                    } else if(null == event.msg) {
                        logger.info(event.e);
                    } else {
                        logger.info(event.msg, event.e);
                    }
                }
                case DEBUG -> {
                    if (null == event.e) {
                        logger.debug(event.msg);
                    } else if(null == event.msg) {
                        logger.debug(event.e);
                    } else {
                        logger.debug(event.msg, event.e);
                    }
                }
                case TRACE -> {
                    if (null == event.e) {
                        logger.trace(event.msg);
                    }
                }
            }
        });
        memory.clear();
        if (null == startNormally) {
            logger.freezeAll();
        } else if (!startNormally) {
            logger.holdAll();
        }
    }

    @Override
    public LogLevel getLevel() {
        return LogLevel.TRACE;
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

    public void log(final LogLevel logLevel, final String msg) {
        memory.add(new LogEvent(logLevel, msg, null));
    }

    public void log(final LogLevel logLevel, final String msg, final Throwable e) {
        memory.add(new LogEvent(logLevel, msg, e));
    }

    private void log(final LogLevel logLevel, final Throwable e) {
        memory.add(new LogEvent(logLevel, null, e));
    }
}