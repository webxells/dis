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
package com.webxells.dis.logging.simple;

import com.webxells.dis.api.LogManager;
import com.webxells.dis.api.Logger;
import com.webxells.dis.api.Logger.LogLevel;
import com.webxells.dis.logging.simple.appender.Appender;
import com.webxells.dis.logging.simple.freeloader.Freeloader;
import com.webxells.dis.logging.simple.internal.JsonParser;
import com.webxells.dis.logging.simple.internal.ConcurrentLoggingDirector;
import com.webxells.dis.logging.simple.internal.LoggingDirector;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class DisLogManager implements LogManager {
    private final List<Appender> appenders;
    private final LogLevel defaultLevel;
    private final String defaultPattern;
    private final Map<String, LogLevel> defaultExceptions;
    private final LoggingDirector director;

    public DisLogManager(final InputStream file, final int waitTime) throws IOException {
        final JsonParser jsonParser = new JsonParser(file);
        defaultLevel = jsonParser.getAsEnum("defaultLevel", LogLevel.class)
                .orElse(LogLevel.INFO);
        defaultPattern = jsonParser.getAsString("defaultPattern")
                .orElse("$date [$thread] $level $name - $msg$exception");
        defaultExceptions = jsonParser.getAsEnumMap("defaultExceptions", LogLevel.class);
        appenders = jsonParser.getAppenders("appenders");
        appenders.forEach(a -> a.fillDefaults(this));
        director = jsonParser.getAsBoolean("synchronLogging", false) ?
                new LoggingDirector(this) :
                new ConcurrentLoggingDirector(this, waitTime);
        director.registerFreeloaders(jsonParser.getAsEnumList("freeloaders", Freeloader.Type.class));
    }

    public DisLogManager(final DisLogManagerConfig config, final int waitTime) {
        defaultLevel = config.defaultLevel();
        defaultPattern = config.defaultPattern();
        defaultExceptions = config.defaultExceptions();
        appenders = config.appenders();
        appenders.forEach(a -> a.fillDefaults(this));
        director = config.synchronLogging() ?
                new LoggingDirector(this) : new ConcurrentLoggingDirector(this, waitTime);
        director.registerFreeloaders(config.freeloaders());
    }

    @Override
    public Logger get(final String name) {
        return director.newLogger(name);
    }

    @Override
    public Logger get(final Class clazz) {
        return get(clazz.getName());
    }

    public List<Appender> getAppenders() {
        return appenders;
    }

    public LogLevel getDefaultLevel() {
        return defaultLevel;
    }

    public String getDefaultPattern() {
        return defaultPattern;
    }

    public Map<String, LogLevel> getDefaultExceptions() {
        return defaultExceptions;
    }
}