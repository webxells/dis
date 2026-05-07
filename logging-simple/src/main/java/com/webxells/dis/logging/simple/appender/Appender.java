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
package com.webxells.dis.logging.simple.appender;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.simple.DisLogManager;
import com.webxells.dis.logging.simple.Event;
import java.util.Map;

public interface Appender {
    public enum REGISTERED_TYPES {
        FILE(FileAppender.class), SYSTEM(PrinterStreamAppender.class);

        private final Class<? extends Appender> clazz;

        REGISTERED_TYPES(final Class<? extends Appender> clazz) {
            this.clazz = clazz;
        }

        public Class<? extends Appender> clazz() {
            return clazz;
        }
    }

    String getPattern();

    Logger.LogLevel getLogLevel();

    Map<String, Logger.LogLevel> getExceptions();

    void fillDefaults(final DisLogManager disLogManager);

    void write(final Event event);

    void forceWrite(final Event event);

    void writeRaw(final String message);
}