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


import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class NativeLoggerAdapter extends LogManager implements Freeloader {

    class NativeLogger extends Logger {
        private final com.webxells.dis.api.Logger logger;

        NativeLogger(final String name) {
            this(String.format("%s.%s", "N", name), null);
        }

        NativeLogger(final String name, final String resourceBundleName) {
            super(null, null);
            loggerNames.add(name);
            logger = LoggerProxyFactory.logger(name);
        }

        public void log(final Level level, final String msg, final Object... params) {
            super.log(level, msg, params);
        }

        @Override
        public boolean isLoggable(final Level level) {
            return logger.has(toDisLevel(level));
        }

        @Override
        public void log(final Level level, final String msg) {
            logger.log(toDisLevel(level), msg);
        }

        @Override
        public void log(final LogRecord record) {
            final Throwable exception = record.getThrown();
            final Object[] parameters = record.getParameters();
            if (null !=  parameters) {
                formatParamString(record);
            }
            if (null == exception) {
                if (null == parameters) {
                    log(record.getLevel(), record.getMessage());
                } else {
                    logger.log(toDisLevel(record.getLevel()), record.getMessage(),  parameters);
                }
            } else {
                if (null ==  parameters) {
                    log(record.getLevel(), record.getMessage(), exception);
                } else {
                    logger.log(toDisLevel(record.getLevel()), record.getMessage(),  parameters, exception);
                }
            }
        }

        private void formatParamString(final LogRecord record) {
            final int size = record.getParameters().length;
            if (0 < size) {
                record.setMessage(replaceLogMessage(record.getMessage().toCharArray()));
            }
        }

        private String replaceLogMessage(final char[] message) {
            final StringBuilder result = new StringBuilder();
            for (int i = 0, m = message.length; i < m; i++) {
                if ('{' == message[i]) {
                    if ('}' == message[i + 1]) {
                        result.append("%s");
                        i++;
                        continue;
                    } else {
                        final String index = getNextNumIndex(message, i + 1);
                        if (null != index) {
                            result.append(String.format("%%%d$s", Integer.parseInt(index) + 1));
                            i+= 1 + index.length();
                            continue;
                        }
                    }
                } else if ('%' == message[i]) {
                    result.append("%");
                }
                result.append(message[i]);
            }
            return result.toString();
        }

        private String getNextNumIndex(final char[] message, int i) {
            final int start = i;
            while (Character.isDigit(message[i])) {
              i++;
            }
            return start == i ? null : new String(Arrays.copyOfRange(message, start, i));
        }

        @Override
        public void log(final Level level, final Supplier<String> msgSupplier) {
            log(level, msgSupplier.get());
        }

        @Override
        public void log(final Level level, final String msg, final Throwable thrown) {
            logger.log(toDisLevel(level), msg, thrown);
        }

        private com.webxells.dis.api.Logger.LogLevel toDisLevel(final Level level) {
            return switch (level.intValue()) {
                // OFF
                case Integer.MAX_VALUE -> com.webxells.dis.api.Logger.LogLevel.OFF;
                // SEVERE
                case 1000 -> com.webxells.dis.api.Logger.LogLevel.FATAL;
                // WARNING
                case 900 -> com.webxells.dis.api.Logger.LogLevel.WARN;
                // INFO, CONFIG
                case 700, 800 -> com.webxells.dis.api.Logger.LogLevel.INFO;
                // FINE
                case 500 -> com.webxells.dis.api.Logger.LogLevel.DEBUG;
                //FINER, FINEST, ALL
                case 400, 300, Integer.MIN_VALUE  -> com.webxells.dis.api.Logger.LogLevel.TRACE;
                default ->
                    throw new IllegalStateException("Unexpected value: " + level.getName());
            };
        }

        @Override
        public void log(final Level level, final Throwable thrown, final Supplier<String> msgSupplier) {
            log(level, msgSupplier.get(), thrown);
        }
    }

    private final List<String> loggerNames = new LinkedList<>();

    @Override
    public void register() {
        System.setProperty("java.util.logging.manager", NativeLoggerAdapter.class.getName());
        try {
            this.readConfiguration(new ByteArrayInputStream("handlers=java.util.logging.ConsoleHandler\n.level=OFF".getBytes()));
        } catch (final IOException e) {
            throw new RuntimeException("could not reset config", e);
        }
    }

    @Override
    public void reset() throws SecurityException { }

    @Override
    public boolean addLogger(final Logger logger) {
        return false;
    }

    @Override
    public Logger getLogger(final String name) {
        return new NativeLogger(name);
    }

    @Override
    public Enumeration<String> getLoggerNames() {
        return Collections.enumeration(loggerNames);
    }
}