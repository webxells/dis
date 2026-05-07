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

import java.util.function.Supplier;
import org.slf4j.Marker;

public interface LoggingEventBuilder {
    LoggingEventBuilder setCause(Throwable cause);

    LoggingEventBuilder addMarker(Marker marker);

    LoggingEventBuilder addArgument(Object p);

    LoggingEventBuilder addArgument(Supplier<?> objectSupplier);

    LoggingEventBuilder addKeyValue(String key, Object value);

    LoggingEventBuilder addKeyValue(String key, Supplier<Object> valueSupplier);

    LoggingEventBuilder setMessage(String message);

    LoggingEventBuilder setMessage(Supplier<String> messageSupplier);

    void log();

    void log(String message);

    void log(String message, Object arg);

    void log(String message, Object arg0, Object arg1);

    void log(String message, Object... args);

    void log(Supplier<String> messageSupplier);
}