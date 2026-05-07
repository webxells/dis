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
package com.webxells.dis.logging.simple.freeloader;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class SystemLoggerAdapter extends System.LoggerFinder implements Freeloader {
    public static class SystemLogger implements System.Logger {
        private final String name;
        private final Logger logger;

        SystemLogger(final String name) {
            this.name = name;
            logger = LoggerProxyFactory.logger("S.".concat(name));
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean isLoggable(final Level level) {
            return logger.has(toDisLevel(level));
        }

        @Override
        public void log(final Level level, final ResourceBundle bundle, final String msg, final Throwable thrown) {
            if (null == thrown) {
                logIt(level, msg);
            } else {
                logIt(level, msg, thrown);
            }
        }

        @Override
        public void log(final Level level, final ResourceBundle bundle, final String format, final Object... params) {
            try {
                logIt(level, MessageFormat.format(format, params));
            } catch (final IllegalArgumentException e) {
                logIt(level, String.format("MSG: %s, PARAMS: %s", format,
                        null == params ? "null" :
                                Arrays.stream(params).map(String::valueOf).collect(Collectors.joining(","))));
            }
        }

        public void logIt(final Level level, final String msg) {
            logger.log(toDisLevel(level), msg);
        }

        public void logIt(final Level level, final String msg, final Throwable thrown) {
            logger.log(toDisLevel(level), msg, thrown);
        }

        private LogLevel toDisLevel(final Level level) {
            return switch (level) {
                case ALL, TRACE -> LogLevel.TRACE;
                case DEBUG -> LogLevel.DEBUG;
                case INFO -> LogLevel.INFO;
                case WARNING -> LogLevel.WARN;
                case ERROR -> LogLevel.ERROR;
                case OFF -> LogLevel.OFF;
            };
        }
    }

    private final Map<String, SystemLogger> instances = new ConcurrentHashMap<>();

    @Override
    public void register() { }

    @Override
    public System.Logger getLogger(final String name, final Module module) {
        return instances.computeIfAbsent(name, SystemLogger::new);
    }
}