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
package org.slf4j.spi;

import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;
import org.slf4j.Marker;
import org.slf4j.event.Level;

public class SimpleLoggingEventBuilder implements LoggingEventBuilder {
    private final Level level;
    private final LocationAwareLogger logger;
    private final Queue<String> parameters = new ConcurrentLinkedQueue<>();

    private String message = "";
    private Throwable cause;

    public SimpleLoggingEventBuilder(final Level level, final LocationAwareLogger logger) {
        this.level = level;
        this.logger = logger;
    }

    @Override
    public LoggingEventBuilder setCause(final Throwable cause) {
        this.cause = cause;
        return this;
    }

    @Override
    public LoggingEventBuilder addMarker(final Marker marker) {
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(final Object p) {
        parameters.add(String.valueOf(p));
        return this;
    }

    @Override
    public LoggingEventBuilder addArgument(final Supplier<?> objectSupplier) {
        addArgument(objectSupplier.get());
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(final String key, final Object value) {
        addArgument(String.format("%s: %s", key, value));
        return this;
    }

    @Override
    public LoggingEventBuilder addKeyValue(final String key, final Supplier<Object> valueSupplier) {
        addArgument(String.format("%s: %s", key, valueSupplier.get()));
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(final String message) {
        this.message = message;
        return this;
    }

    @Override
    public LoggingEventBuilder setMessage(final Supplier<String> messageSupplier) {
        this.message = messageSupplier.get();
        return this;
    }

    @Override
    public void log() {
        if (logger.isEnabledForLevel(level)) {
            logger.log(null, null, level.toInt(),
                    String.format("%s: %s", message, String.join(", ", parameters)), null, cause);
        }
    }

    @Override
    public void log(final String message) {
        setMessage(message);
        log();
    }

    @Override
    public void log(final String message, final Object arg) {
        addArgument(arg);
        setMessage(message);
        log();
    }

    @Override
    public void log(final String message, final Object arg0, final Object arg1) {
        addArgument(arg0);
        addArgument(arg1);
        setMessage(message);
        log();
    }

    @Override
    public void log(final String message, final Object... args) {
        Arrays.stream(args).forEach(this::addArgument);
        setMessage(message);
        log();
    }

    @Override
    public void log(final Supplier<String> messageSupplier) {
        setMessage(messageSupplier);
        log();
    }

}