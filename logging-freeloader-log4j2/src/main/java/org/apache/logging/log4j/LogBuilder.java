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

import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.Supplier;

public interface LogBuilder {
    LogBuilder NOOP = new LogBuilder() {  };

    default LogBuilder withMarker(Marker marker) {
        return this;
    }

    default LogBuilder withThrowable(Throwable throwable) {
        return this;
    }

    default LogBuilder withLocation() {
        return this;
    }

    default LogBuilder withLocation(StackTraceElement location) {
        return this;
    }

    default void log(CharSequence message) {
        log(message.toString());
    }

    default void log(String message) {
    }

    default void log(String message, Object... params) {
    }

    default void log(String message, Supplier<?>... params) {
    }

    default void log(Message message) {
    }

    default void log(Supplier<Message> messageSupplier) {
    }

    default void log(Object message) {
    }

    default void log(String message, Object p0) {
    }

    default void log(String message, Object p0, Object p1) {
    }

    default void log(String message, Object p0, Object p1, Object p2) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
    }

    default void log(String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
    }

    default void log() {
    }
}