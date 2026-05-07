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
package org.apache.logging.log4j.spi;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.util.MessageSupplier;
import org.apache.logging.log4j.util.Supplier;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;

public interface ExtendedLogger extends Logger {
    boolean isEnabled(Level level, Marker marker, Message message, Throwable t);

    boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t);

    boolean isEnabled(Level level, Marker marker, Object message, Throwable t);

    boolean isEnabled(Level level, Marker marker, String message, Throwable t);

    boolean isEnabled(Level level, Marker marker, String message);

    boolean isEnabled(Level level, Marker marker, String message, Object... params);

    boolean isEnabled(Level level, Marker marker, String message, Object p0);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8);

    boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9);

    void logIfEnabled(String fqcn, Level level, Marker marker, Message message, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, CharSequence message, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, Object message, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object... params);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9);

    void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, MessageSupplier msgSupplier, Throwable t);

    void logIfEnabled(String fqcn, Level level, Marker marker, String message, Supplier<?>... paramSuppliers);

    void logIfEnabled(String fqcn, Level level, Marker marker, Supplier<?> msgSupplier, Throwable t);
}