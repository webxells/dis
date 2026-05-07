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
package org.apache.logging.log4j;

import java.util.Optional;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

public class SimpleLogBuilder implements LogBuilder {
    private final Level level;
    private final Logger logger;

    private Throwable throwable;

    public SimpleLogBuilder(final Level level, final Logger logger) {
        this.level = level;
        this.logger = logger;
    }

    @Override
    public LogBuilder withThrowable(final Throwable throwable) {
        this.throwable = throwable;
        return this;
    }

    @Override
    public void log(final String message) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a), () -> logger.log(level, message));
    }

    @Override
    public void log(final String message, final Object... params) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, params, a), () -> logger.log(level, message, params));
    }

    @Override
    public void log(final String message, final Supplier<?>... params) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, params, a), () -> logger.log(level, message, params));
    }

    @Override
    public void log(final Message message) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a), () -> logger.log(level, message));
    }

    @Override
    public void log(final Supplier<Message> messageSupplier) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, messageSupplier, a), () -> logger.log(level, messageSupplier));
    }

    @Override
    public void log(final Object message) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a), () -> logger.log(level, message));
    }

    @Override
    public void log(final String message, final Object p0) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0), () -> logger.log(level, message, p0));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1), () -> logger.log(level, message, p0, 1));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2), () -> logger.log(level, message, p0, p1, p2));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3), () -> logger.log(level, message, p0, p1, p2, p3));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4), () -> logger.log(level, message, p0, p1, p2, p3, p4));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4, p5), () -> logger.log(level, message, p0, p1, p2, p3, p4, p5));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4, p5, p6), () -> logger.log(level, message, p0, p1, p2, p3, p4, p5, p6));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4, p5, p6, p7), () -> logger.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4, p5, p6, p7, p8), () -> logger.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7, p8));
    }

    @Override
    public void log(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        Optional.ofNullable(throwable)
                .ifPresentOrElse(a -> logger.log(level, message, a, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9), () -> logger.log(level, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9));
    }

    @Override
    public void log() {
        Optional.ofNullable(throwable)
                .ifPresent(a -> logger.log(level, a));
    }
}