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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.apache.logging.log4j.message.EntryMessage;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;

public class DisLogger4Log4J implements ExtendedLogger {
    private final Logger logger;
    private final String name;
    private final Level level;

    public DisLogger4Log4J(final String name) {
        this.name = name;
        this.logger = LoggerProxyFactory.logger("L.".concat(name));
        level = mapLevel();
    }

    private Level mapLevel() {
        return switch (logger.getLevel()) {
            case OFF -> Level.OFF;
            case FATAL -> Level.FATAL;
            case ERROR -> Level.ERROR;
            case WARN -> Level.WARN;
            case INFO -> Level.INFO;
            case DEBUG -> Level.DEBUG;
            case TRACE -> Level.TRACE;
        };
    }

    @Override
    public void catching(final Level level, final Throwable throwable) {

    }

    @Override
    public void catching(final Throwable throwable) {

    }

    @Override
    public void debug(final Marker marker, final Message message) {
        debug(message);
    }

    @Override
    public void debug(final Marker marker, final Message message, final Throwable throwable) {
        debug(message, throwable);
    }

    @Override
    public void debug(final Marker marker, final MessageSupplier messageSupplier) {
        debug(messageSupplier.get());
    }

    @Override
    public void debug(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        debug(messageSupplier.get(), throwable);
    }

    @Override
    public void debug(final Marker marker, final CharSequence message) {
        debug(message);
    }

    @Override
    public void debug(final Marker marker, final CharSequence message, final Throwable throwable) {
        debug(message, throwable);
    }

    @Override
    public void debug(final Marker marker, final Object message) {
        debug(message);
    }

    @Override
    public void debug(final Marker marker, final Object message, final Throwable throwable) {
        debug(message, throwable);
    }

    @Override
    public void debug(final Marker marker, final String message) {
        debug(message);
    }

    @Override
    public void debug(final Marker marker, final String message, final Object... params) {
        debug(message, params);
    }

    @Override
    public void debug(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        debug(message, paramSuppliers);
    }

    @Override
    public void debug(final Marker marker, final String message, final Throwable throwable) {
        debug(message, throwable);
    }

    @Override
    public void debug(final Marker marker, final Supplier<?> messageSupplier) {
        debug(messageSupplier);
    }

    @Override
    public void debug(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        debug(messageSupplier, throwable);
    }

    @Override
    public void debug(final Message message) {
        debug(message.getFormattedMessage());
    }

    @Override
    public void debug(final Message message, final Throwable throwable) {
        debug(message.getFormattedMessage(), throwable);
    }

    @Override
    public void debug(final MessageSupplier messageSupplier) {
        debug(messageSupplier.get());
    }

    @Override
    public void debug(final MessageSupplier messageSupplier, final Throwable throwable) {
        debug(messageSupplier.get(), throwable);
    }

    @Override
    public void debug(final CharSequence message) {
        debug(message.toString());
    }

    @Override
    public void debug(final CharSequence message, final Throwable throwable) {
        debug(message.toString(), throwable);
    }

    @Override
    public void debug(final Object message) {
        debug(String.valueOf(message));
    }

    @Override
    public void debug(final Object message, final Throwable throwable) {
        debug(String.valueOf(message), throwable);
    }

    @Override
    public void debug(final String message) {
        logger.d(message);
    }

    @Override
    public void debug(final String message, final Object... params) {
        logger.d(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void debug(final String message, final Throwable throwable) {
        logger.d(message, throwable);
    }

    private String toString(final Object[] params) {
        return Arrays.stream(params)
                .map(Object::toString)
                .collect(Collectors.joining(", "));
    }

    @Override
    public void debug(final String message, final Supplier<?>... paramSuppliers) {
        debug(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void debug(final Supplier<?> messageSupplier) {
        debug(messageSupplier.get());
    }

    @Override
    public void debug(final Supplier<?> messageSupplier, final Throwable throwable) {
        debug(messageSupplier.get(), throwable);
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0) {
        debug(message, new Object[]{ p0 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1) {
        debug(message, new Object[]{ p0, p1 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        debug(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        debug(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void debug(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void debug(final String message, final Object p0) {
        debug(message, new Object[]{ p0 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1) {
        debug(message, new Object[]{ p0, p1 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2) {
        debug(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        debug(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void debug(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        debug(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void entry() { }

    @Override
    public void entry(final Object... params) { }

    @Override
    public void error(final Marker marker, final Message message) {
        error(message);
    }

    @Override
    public void error(final Marker marker, final Message message, final Throwable throwable) {
        error(message, throwable);
    }

    @Override
    public void error(final Marker marker, final MessageSupplier messageSupplier) {
        error(messageSupplier.get());
    }

    @Override
    public void error(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        error(messageSupplier.get(), throwable);
    }

    @Override
    public void error(final Marker marker, final CharSequence message) {
        error(message);
    }

    @Override
    public void error(final Marker marker, final CharSequence message, final Throwable throwable) {
        error(message, throwable);
    }

    @Override
    public void error(final Marker marker, final Object message) {
        error(message);
    }

    @Override
    public void error(final Marker marker, final Object message, final Throwable throwable) {
        error(message, throwable);
    }

    @Override
    public void error(final Marker marker, final String message) {
        error(message);
    }

    @Override
    public void error(final Marker marker, final String message, final Object... params) {
        error(message, params);
    }

    @Override
    public void error(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        error(message, paramSuppliers);
    }

    @Override
    public void error(final Marker marker, final String message, final Throwable throwable) {
        error(message, throwable);
    }

    @Override
    public void error(final Marker marker, final Supplier<?> messageSupplier) {
        error(messageSupplier);
    }

    @Override
    public void error(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        error(messageSupplier, throwable);
    }

    @Override
    public void error(final Message message) {
        error(message.getFormattedMessage());
    }

    @Override
    public void error(final Message message, final Throwable throwable) {
        error(message.getFormattedMessage(), throwable);
    }

    @Override
    public void error(final MessageSupplier messageSupplier) {
        error(messageSupplier.get());
    }

    @Override
    public void error(final MessageSupplier messageSupplier, final Throwable throwable) {
        error(messageSupplier.get(), throwable);
    }

    @Override
    public void error(final CharSequence message) {
        error(message.toString());
    }

    @Override
    public void error(final CharSequence message, final Throwable throwable) {
        error(message.toString(), throwable);
    }

    @Override
    public void error(final Object message) {
        error(String.valueOf(message));
    }

    @Override
    public void error(final Object message, final Throwable throwable) {
        error(String.valueOf(message), throwable);
    }

    @Override
    public void error(final String message) {
        logger.e(message);
    }

    @Override
    public void error(final String message, final Object... params) {
        logger.e(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void error(final String message, final Throwable throwable) {
        logger.e(message, throwable);
    }

    @Override
    public void error(final String message, final Supplier<?>... paramSuppliers) {
        error(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void error(final Supplier<?> messageSupplier) {
        error(messageSupplier.get());
    }

    @Override
    public void error(final Supplier<?> messageSupplier, final Throwable throwable) {
        error(messageSupplier.get(), throwable);
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0) {
        error(message, new Object[]{ p0 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1) {
        error(message, new Object[]{ p0, p1 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        error(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        error(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        error(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void error(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void error(final String message, final Object p0) {
        error(message, new Object[]{ p0 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1) {
        error(message, new Object[]{ p0, p1 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2) {
        error(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        error(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        error(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void error(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        error(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void exit() { }

    @Override
    public <R> R exit(final R result) {
        return result;
    }

    @Override
    public void fatal(final Marker marker, final Message message) {
        fatal(message);
    }

    @Override
    public void fatal(final Marker marker, final Message message, final Throwable throwable) {
        fatal(message, throwable);
    }

    @Override
    public void fatal(final Marker marker, final MessageSupplier messageSupplier) {
        fatal(messageSupplier.get());
    }

    @Override
    public void fatal(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        fatal(messageSupplier.get(), throwable);
    }

    @Override
    public void fatal(final Marker marker, final CharSequence message) {
        fatal(message);
    }

    @Override
    public void fatal(final Marker marker, final CharSequence message, final Throwable throwable) {
        fatal(message, throwable);
    }

    @Override
    public void fatal(final Marker marker, final Object message) {
        fatal(message);
    }

    @Override
    public void fatal(final Marker marker, final Object message, final Throwable throwable) {
        fatal(message, throwable);
    }

    @Override
    public void fatal(final Marker marker, final String message) {
        fatal(message);
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object... params) {
        fatal(message, params);
    }

    @Override
    public void fatal(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        fatal(message, paramSuppliers);
    }

    @Override
    public void fatal(final Marker marker, final String message, final Throwable throwable) {
        fatal(message, throwable);
    }

    @Override
    public void fatal(final Marker marker, final Supplier<?> messageSupplier) {
        fatal(messageSupplier);
    }

    @Override
    public void fatal(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        fatal(messageSupplier, throwable);
    }

    @Override
    public void fatal(final Message message) {
        fatal(message.getFormattedMessage());
    }

    @Override
    public void fatal(final Message message, final Throwable throwable) {
        fatal(message.getFormattedMessage(), throwable);
    }

    @Override
    public void fatal(final MessageSupplier messageSupplier) {
        fatal(messageSupplier.get());
    }

    @Override
    public void fatal(final MessageSupplier messageSupplier, final Throwable throwable) {
        fatal(messageSupplier.get(), throwable);
    }

    @Override
    public void fatal(final CharSequence message) {
        fatal(message.toString());
    }

    @Override
    public void fatal(final CharSequence message, final Throwable throwable) {
        fatal(message.toString(), throwable);
    }

    @Override
    public void fatal(final Object message) {
        fatal(String.valueOf(message));
    }

    @Override
    public void fatal(final Object message, final Throwable throwable) {
        fatal(String.valueOf(message), throwable);
    }

    @Override
    public void fatal(final String message) {
        logger.f(message);
    }

    @Override
    public void fatal(final String message, final Object... params) {
        logger.f(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void fatal(final String message, final Throwable throwable) {
        logger.f(message, throwable);
    }

    @Override
    public void fatal(final String message, final Supplier<?>... paramSuppliers) {
        fatal(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void fatal(final Supplier<?> messageSupplier) {
        fatal(messageSupplier.get());
    }

    @Override
    public void fatal(final Supplier<?> messageSupplier, final Throwable throwable) {
        fatal(messageSupplier.get(), throwable);
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0) {
        fatal(message, new Object[]{ p0 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1) {
        fatal(message, new Object[]{ p0, p1 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        fatal(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        fatal(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void fatal(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void fatal(final String message, final Object p0) {
        fatal(message, new Object[]{ p0 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1) {
        fatal(message, new Object[]{ p0, p1 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2) {
        fatal(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        fatal(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void fatal(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        fatal(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public Level getLevel() {
        return level;
    }

    @Override
    public <MF extends MessageFactory> MF getMessageFactory() {
        return (MF) new MessageFactory.SimpleMessageFactory();
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void info(final Marker marker, final Message message) {
        info(message);
    }

    @Override
    public void info(final Marker marker, final Message message, final Throwable throwable) {
        info(message, throwable);
    }

    @Override
    public void info(final Marker marker, final MessageSupplier messageSupplier) {
        info(messageSupplier.get());
    }

    @Override
    public void info(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        info(messageSupplier.get(), throwable);
    }

    @Override
    public void info(final Marker marker, final CharSequence message) {
        info(message);
    }

    @Override
    public void info(final Marker marker, final CharSequence message, final Throwable throwable) {
        info(message, throwable);
    }

    @Override
    public void info(final Marker marker, final Object message) {
        info(message);
    }

    @Override
    public void info(final Marker marker, final Object message, final Throwable throwable) {
        info(message, throwable);
    }

    @Override
    public void info(final Marker marker, final String message) {
        info(message);
    }

    @Override
    public void info(final Marker marker, final String message, final Object... params) {
        info(message, params);
    }

    @Override
    public void info(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        info(message, paramSuppliers);
    }

    @Override
    public void info(final Marker marker, final String message, final Throwable throwable) {
        info(message, throwable);
    }

    @Override
    public void info(final Marker marker, final Supplier<?> messageSupplier) {
        info(messageSupplier);
    }

    @Override
    public void info(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        info(messageSupplier, throwable);
    }

    @Override
    public void info(final Message message) {
        info(message.getFormattedMessage());
    }

    @Override
    public void info(final Message message, final Throwable throwable) {
        info(message.getFormattedMessage(), throwable);
    }

    @Override
    public void info(final MessageSupplier messageSupplier) {
        info(messageSupplier.get());
    }

    @Override
    public void info(final MessageSupplier messageSupplier, final Throwable throwable) {
        info(messageSupplier.get(), throwable);
    }

    @Override
    public void info(final CharSequence message) {
        info(message.toString());
    }

    @Override
    public void info(final CharSequence message, final Throwable throwable) {
        info(message.toString(), throwable);
    }

    @Override
    public void info(final Object message) {
        info(String.valueOf(message));
    }

    @Override
    public void info(final Object message, final Throwable throwable) {
        info(String.valueOf(message), throwable);
    }

    @Override
    public void info(final String message) {
        logger.i(message);
    }

    @Override
    public void info(final String message, final Object... params) {
        logger.i(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void info(final String message, final Throwable throwable) {
        logger.i(message, throwable);
    }

    @Override
    public void info(final String message, final Supplier<?>... paramSuppliers) {
        info(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void info(final Supplier<?> messageSupplier) {
        info(messageSupplier.get());
    }

    @Override
    public void info(final Supplier<?> messageSupplier, final Throwable throwable) {
        info(messageSupplier.get(), throwable);
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0) {
        info(message, new Object[]{ p0 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1) {
        info(message, new Object[]{ p0, p1 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        info(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        info(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        info(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void info(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void info(final String message, final Object p0) {
        info(message, new Object[]{ p0 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1) {
        info(message, new Object[]{ p0, p1 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2) {
        info(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        info(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        info(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void info(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        info(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public boolean isDebugEnabled() {
        return isEnabled(Level.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(final Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public boolean isEnabled(final Level level) {
        return level.isMoreSpecificThan(this.level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker) {
        return isEnabled(level);
    }

    @Override
    public boolean isErrorEnabled() {
        return isEnabled(Level.ERROR);
    }

    @Override
    public boolean isErrorEnabled(final Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public boolean isFatalEnabled() {
        return isEnabled(Level.FATAL);
    }

    @Override
    public boolean isFatalEnabled(final Marker marker) {
        return isFatalEnabled();
    }

    @Override
    public boolean isInfoEnabled() {
        return isEnabled(Level.INFO);
    }

    @Override
    public boolean isInfoEnabled(final Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public boolean isTraceEnabled() {
        return isEnabled(Level.TRACE);
    }

    @Override
    public boolean isTraceEnabled(final Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public boolean isWarnEnabled() {
        return isEnabled(Level.WARN);
    }

    @Override
    public boolean isWarnEnabled(final Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void log(final Level level, final Marker marker, final Message message) {
        log(level, message);
    }

    @Override
    public void log(final Level level, final Marker marker, final Message message, final Throwable throwable) {
        log(level, message, throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final MessageSupplier messageSupplier) {
        log(level, messageSupplier.get());
    }

    @Override
    public void log(final Level level, final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        log(level, messageSupplier.get(), throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final CharSequence message) {
        log(level, message);
    }

    @Override
    public void log(final Level level, final Marker marker, final CharSequence message, final Throwable throwable) {
        log(level, message, throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final Object message) {
        log(level, message);
    }

    @Override
    public void log(final Level level, final Marker marker, final Object message, final Throwable throwable) {
        log(level, message, throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final String message) {
        log(level, message);
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object... params) {
        log(level, message, params);
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        log(level, message, paramSuppliers);
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Throwable throwable) {
        log(level, message, throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final Supplier<?> messageSupplier) {
        log(level, messageSupplier);
    }

    @Override
    public void log(final Level level, final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        log(level, messageSupplier, throwable);
    }

    @Override
    public void log(final Level level, final Message message) {
        log(level, message.getFormattedMessage());
    }

    @Override
    public void log(final Level level, final Message message, final Throwable throwable) {
        log(level, message.getFormattedMessage(), throwable);
    }

    @Override
    public void log(final Level level, final MessageSupplier messageSupplier) {
        log(level, messageSupplier.get());
    }

    @Override
    public void log(final Level level, final MessageSupplier messageSupplier, final Throwable throwable) {
        log(level, messageSupplier.get(), throwable);
    }

    @Override
    public void log(final Level level, final CharSequence message) {
        log(level, message.toString());
    }

    @Override
    public void log(final Level level, final CharSequence message, final Throwable throwable) {
        log(level, message.toString(), throwable);
    }

    @Override
    public void log(final Level level, final Object message) {
        log(level, String.valueOf(message));
    }

    @Override
    public void log(final Level level, final Object message, final Throwable throwable) {
        log(level, String.valueOf(message), throwable);
    }

    @Override
    public void log(final Level level, final String message) {
        switch (level.getStandardLevel()) {
            case OFF -> { }
            case FATAL, ALL -> logger.f(message);
            case ERROR -> logger.e(message);
            case WARN -> logger.w(message);
            case INFO -> logger.i(message);
            case DEBUG -> logger.d(message);
            case TRACE -> logger.t(message);
        }
    }

    @Override
    public void log(final Level level, final String message, final Object... params) {
        log(level, String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void log(final Level level, final String message, final Throwable throwable) {
        switch (level.getStandardLevel()) {
            case OFF -> { }
            case FATAL  -> logger.f(message, throwable);
            case ERROR -> logger.e(message, throwable);
            case WARN -> logger.w(message, throwable);
            case INFO -> logger.i(message, throwable);
            case DEBUG -> logger.d(message, throwable);
            case TRACE, ALL -> logger.t(message, throwable);
        }
    }

    @Override
    public void log(final Level level, final String message, final Supplier<?>... paramSuppliers) {
        log(level, message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void log(final Level level, final Supplier<?> messageSupplier) {
        log(level, messageSupplier.get());
    }

    @Override
    public void log(final Level level, final Supplier<?> messageSupplier, final Throwable throwable) {
        log(level, messageSupplier.get(), throwable);
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0) {
        log(level, message, new Object[]{ p0 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1) {
        log(level, message, new Object[]{ p0, p1 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        log(level, message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        log(level, message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void log(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0) {
        log(level, message, new Object[]{ p0 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1) {
        log(level, message, new Object[]{ p0, p1 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2) {
        log(level, message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        log(level, message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void log(final Level level, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        log(level, message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void printf(final Level level, final Marker marker, final String format, final Object... params) {
        printf(level, format, params);
    }

    @Override
    public void printf(final Level level, final String format, final Object... params) {
        log(level, String.format(format, params));
    }

    @Override
    public <T extends Throwable> T throwing(final Level level, final T throwable) {
        log(level, "Exception encountered", throwable);
        return throwable;
    }

    @Override
    public <T extends Throwable> T throwing(final T throwable) {
        error("Exception encountered", throwable);
        return throwable;
    }

    @Override
    public void trace(final Marker marker, final Message message) {
        trace(message);
    }

    @Override
    public void trace(final Marker marker, final Message message, final Throwable throwable) {
        trace(message, throwable);
    }

    @Override
    public void trace(final Marker marker, final MessageSupplier messageSupplier) {
        trace(messageSupplier.get());
    }

    @Override
    public void trace(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        trace(messageSupplier.get(), throwable);
    }

    @Override
    public void trace(final Marker marker, final CharSequence message) {
        trace(message);
    }

    @Override
    public void trace(final Marker marker, final CharSequence message, final Throwable throwable) {
        trace(message, throwable);
    }

    @Override
    public void trace(final Marker marker, final Object message) {
        trace(message);
    }

    @Override
    public void trace(final Marker marker, final Object message, final Throwable throwable) {
        trace(message, throwable);
    }

    @Override
    public void trace(final Marker marker, final String message) {
        trace(message);
    }

    @Override
    public void trace(final Marker marker, final String message, final Object... params) {
        trace(message, params);
    }

    @Override
    public void trace(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        trace(message, paramSuppliers);
    }

    @Override
    public void trace(final Marker marker, final String message, final Throwable throwable) {
        trace(message, throwable);
    }

    @Override
    public void trace(final Marker marker, final Supplier<?> messageSupplier) {
        trace(messageSupplier);
    }

    @Override
    public void trace(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        trace(messageSupplier, throwable);
    }

    @Override
    public void trace(final Message message) {
        trace(message.getFormattedMessage());
    }

    @Override
    public void trace(final Message message, final Throwable throwable) {
        trace(message.getFormattedMessage(), throwable);
    }

    @Override
    public void trace(final MessageSupplier messageSupplier) {
        trace(messageSupplier.get());
    }

    @Override
    public void trace(final MessageSupplier messageSupplier, final Throwable throwable) {
        trace(messageSupplier.get(), throwable);
    }

    @Override
    public void trace(final CharSequence message) {
        trace(message.toString());
    }

    @Override
    public void trace(final CharSequence message, final Throwable throwable) {
        trace(message.toString(), throwable);
    }

    @Override
    public void trace(final Object message) {
        trace(String.valueOf(message));
    }

    @Override
    public void trace(final Object message, final Throwable throwable) {
        trace(String.valueOf(message), throwable);
    }

    @Override
    public void trace(final String message) {
        logger.t(message);
    }

    @Override
    public void trace(final String message, final Object... params) {
        logger.t(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void trace(final String message, final Throwable throwable) {
        logger.t(message, throwable);
    }

    @Override
    public void trace(final String message, final Supplier<?>... paramSuppliers) {
        trace(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void trace(final Supplier<?> messageSupplier) {
        trace(messageSupplier.get());
    }

    @Override
    public void trace(final Supplier<?> messageSupplier, final Throwable throwable) {
        trace(messageSupplier.get(), throwable);
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0) {
        trace(message, new Object[]{ p0 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1) {
        trace(message, new Object[]{ p0, p1 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        trace(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        trace(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void trace(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void trace(final String message, final Object p0) {
        trace(message, new Object[]{ p0 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1) {
        trace(message, new Object[]{ p0, p1 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2) {
        trace(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        trace(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void trace(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        trace(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public EntryMessage traceEntry() {
        return new EntryMessage.SimpleEntryMessage("");
    }

    @Override
    public EntryMessage traceEntry(final String format, final Object... params) {
        return traceEntry(String.format(format, params));
    }

    @Override
    public EntryMessage traceEntry(final Supplier<?>... paramSuppliers) {
        return traceEntry(Arrays.stream(paramSuppliers).map(Supplier::get).map(Object::toString).collect(Collectors.joining(", ")));
    }

    @Override
    public EntryMessage traceEntry(final String format, final Supplier<?>... paramSuppliers) {
        return traceEntry(format, Arrays.stream(paramSuppliers).map(Supplier::get).toArray());
    }

    @Override
    public EntryMessage traceEntry(final Message message) {
        return traceEntry(message.getFormat());
    }

    private EntryMessage traceEntry(final String message) {
        return new EntryMessage.SimpleEntryMessage(message);
    }

    @Override
    public void traceExit() { }

    @Override
    public <R> R traceExit(final R result) {
        return result;
    }

    @Override
    public <R> R traceExit(final String format, final R result) {
        return result;
    }

    @Override
    public void traceExit(final EntryMessage message) {

    }

    @Override
    public <R> R traceExit(final EntryMessage message, final R result) {
        return result;
    }

    @Override
    public <R> R traceExit(final Message message, final R result) {
        return result;
    }

    @Override
    public void warn(final Marker marker, final Message message) {
        warn(message);
    }

    @Override
    public void warn(final Marker marker, final Message message, final Throwable throwable) {
        warn(message, throwable);
    }

    @Override
    public void warn(final Marker marker, final MessageSupplier messageSupplier) {
        warn(messageSupplier.get());
    }

    @Override
    public void warn(final Marker marker, final MessageSupplier messageSupplier, final Throwable throwable) {
        warn(messageSupplier.get(), throwable);
    }

    @Override
    public void warn(final Marker marker, final CharSequence message) {
        warn(message);
    }

    @Override
    public void warn(final Marker marker, final CharSequence message, final Throwable throwable) {
        warn(message, throwable);
    }

    @Override
    public void warn(final Marker marker, final Object message) {
        warn(message);
    }

    @Override
    public void warn(final Marker marker, final Object message, final Throwable throwable) {
        warn(message, throwable);
    }

    @Override
    public void warn(final Marker marker, final String message) {
        warn(message);
    }

    @Override
    public void warn(final Marker marker, final String message, final Object... params) {
        warn(message, params);
    }

    @Override
    public void warn(final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        warn(message, paramSuppliers);
    }

    @Override
    public void warn(final Marker marker, final String message, final Throwable throwable) {
        warn(message, throwable);
    }

    @Override
    public void warn(final Marker marker, final Supplier<?> messageSupplier) {
        warn(messageSupplier);
    }

    @Override
    public void warn(final Marker marker, final Supplier<?> messageSupplier, final Throwable throwable) {
        warn(messageSupplier, throwable);
    }

    @Override
    public void warn(final Message message) {
        warn(message.getFormattedMessage());
    }

    @Override
    public void warn(final Message message, final Throwable throwable) {
        warn(message.getFormattedMessage(), throwable);
    }

    @Override
    public void warn(final MessageSupplier messageSupplier) {
        warn(messageSupplier.get());
    }

    @Override
    public void warn(final MessageSupplier messageSupplier, final Throwable throwable) {
        warn(messageSupplier.get(), throwable);
    }

    @Override
    public void warn(final CharSequence message) {
        warn(message.toString());
    }

    @Override
    public void warn(final CharSequence message, final Throwable throwable) {
        warn(message.toString(), throwable);
    }

    @Override
    public void warn(final Object message) {
        warn(String.valueOf(message));
    }

    @Override
    public void warn(final Object message, final Throwable throwable) {
        warn(String.valueOf(message), throwable);
    }

    @Override
    public void warn(final String message) {
        logger.w(message);
    }

    @Override
    public void warn(final String message, final Object... params) {
        logger.w(String.format("%s: %s", message, toString(params)));
    }

    @Override
    public void warn(final String message, final Throwable throwable) {
        logger.w(message, throwable);
    }

    @Override
    public void warn(final String message, final Supplier<?>... paramSuppliers) {
        warn(message, Arrays.stream(paramSuppliers)
                .map(Supplier::get)
                .toArray());
    }

    @Override
    public void warn(final Supplier<?> messageSupplier) {
        warn(messageSupplier.get());
    }

    @Override
    public void warn(final Supplier<?> messageSupplier, final Throwable throwable) {
        warn(messageSupplier.get(), throwable);
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0) {
        warn(message, new Object[]{ p0 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1) {
        warn(message, new Object[]{ p0, p1 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        warn(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        warn(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void warn(final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public void warn(final String message, final Object p0) {
        warn(message, new Object[]{ p0 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1) {
        warn(message, new Object[]{ p0, p1 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2) {
        warn(message, new Object[]{ p0, p1, p2 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        warn(message, new Object[]{ p0, p1, p2, p3 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8 });
    }

    @Override
    public void warn(final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        warn(message, new Object[]{ p0, p1, p2, p3, p4, p5, p6, p7, p8, p9 });
    }

    @Override
    public LogBuilder atTrace() {
        return new SimpleLogBuilder(Level.TRACE, this);
    }

    @Override
    public LogBuilder atDebug() {
        return new SimpleLogBuilder(Level.DEBUG, this);
    }

    @Override
    public LogBuilder atInfo() {
        return new SimpleLogBuilder(Level.INFO, this);
    }

    @Override
    public LogBuilder atWarn() {
        return new SimpleLogBuilder(Level.WARN, this);
    }

    @Override
    public LogBuilder atError() {
        return new SimpleLogBuilder(Level.ERROR, this);
    }

    @Override
    public LogBuilder atFatal() {
        return new SimpleLogBuilder(Level.FATAL, this);
    }

    @Override
    public LogBuilder always() {
        return new SimpleLogBuilder(Level.ALL, this);
    }

    @Override
    public LogBuilder atLevel(final Level level) {
        return new SimpleLogBuilder(level, this);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Message message, final Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final CharSequence message, final Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final Object message, final Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Throwable t) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object... params) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        return isEnabled(level);
    }

    @Override
    public boolean isEnabled(final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        return isEnabled(level);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable t) {
        log(level, marker, message, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final CharSequence message, final Throwable t) {
        log(level, marker, message, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Object message, final Throwable t) {
        log(level, marker, message, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Throwable t) {
        log(level, marker, message, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message) {
        log(level, marker, message);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object... params) {
        log(level, marker, message, params);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0) {
        log(level, marker, message, p0);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1) {
        log(level, marker, message, p0, p1);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2) {
        log(level, marker, message, p0, p1, p2);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3) {
        log(level, marker, message, p0, p1, p2, p3);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4) {
        log(level, marker, message, p0, p1, p2, p3, p4);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5) {
        log(level, marker, message, p0, p1, p2, p3, p4, p5);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6) {
        log(level, marker, message, p0, p1, p2, p3, p4, p5, p6);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7) {
        log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8) {
        log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Object p0, final Object p1, final Object p2, final Object p3, final Object p4, final Object p5, final Object p6, final Object p7, final Object p8, final Object p9) {
        log(level, marker, message, p0, p1, p2, p3, p4, p5, p6, p7, p8, p9);
    }

    @Override
    public void logMessage(final String fqcn, final Level level, final Marker marker, final Message message, final Throwable t) {
        log(level, marker, message, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final MessageSupplier msgSupplier, final Throwable t) {
        log(level, marker, msgSupplier, t);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final String message, final Supplier<?>... paramSuppliers) {
        log(level, marker, message, paramSuppliers);
    }

    @Override
    public void logIfEnabled(final String fqcn, final Level level, final Marker marker, final Supplier<?> msgSupplier, final Throwable t) {
        log(level, marker, msgSupplier, t);
    }
}