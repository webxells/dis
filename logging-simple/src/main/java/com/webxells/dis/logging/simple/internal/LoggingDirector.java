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
package com.webxells.dis.logging.simple.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.simple.DisLogManager;
import com.webxells.dis.logging.simple.Event;
import com.webxells.dis.logging.simple.appender.Appender;
import com.webxells.dis.logging.simple.freeloader.Freeloader;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public class LoggingDirector {
    protected final List<Appender> appenders;

    protected final Logger.LogLevel minLevel;
    protected final Map<String, Logger.LogLevel> defaultExceptions;

    protected Integer parentPassedAwayCircle;
    protected volatile boolean hold;
    protected volatile boolean freeze;

    public static Logger.LogLevel findLongestNameInExceptions(
            final Map<String, Logger.LogLevel> map, final String name) {
        return findLongestMapEntryInExceptions(map, name)
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    private static Optional<Map.Entry<String,Logger.LogLevel>> findLongestMapEntryInExceptions(
            final Map<String, Logger.LogLevel> map, final String name) {
        final int minLength = name.length();
        return getByNameWithMaxLength(map.entrySet().stream()
                .filter(a -> minLength >= a.getKey().length())
                .filter(a -> name.startsWith(a.getKey())));
    }

    private static Optional<Map.Entry<String, Logger.LogLevel>> getByNameWithMaxLength(
            final Stream<Map.Entry<String, Logger.LogLevel>> entryStream) {
        return entryStream
                .min(Comparator.comparingInt(a -> a.getKey().length() * -1));
    }

    public LoggingDirector(final DisLogManager disLogManager) {
        this.appenders = disLogManager.getAppenders();
        defaultExceptions = disLogManager.getDefaultExceptions();
        minLevel = getMinLevel(disLogManager);
    }


    public Logger newLogger(final String name) {
        return new SimpleLogger(name, this);
    }

    public void registerFreeloaders(final List<Freeloader.Type> freeloaders) {
        if (null != freeloaders) {
            freeloaders.forEach(a -> a.create().register());
        }
    }

    public Logger.LogLevel getLowestLevel(final String name) {
        return findExceptionLevelByName(name)
                .orElse(minLevel);
    }

    public void hold() {
        hold = true;
    }

    public void freeze() {
        hold();
        freeze = true;
    }

    public boolean onHold() {
        return hold;
    }

    public void resume() {
        hold = false;
        freeze = false;
    }

    void push(final Logger.LogLevel level, final String name, final String msg, final Throwable e) {
        if (!freeze) {
            appenders.forEach(b -> b.write(new Event(Thread.currentThread(), level, name, msg, e)));
        }
    }

    private Optional<Logger.LogLevel> findExceptionLevelByName(final String name) {
        return getByNameWithMaxLength(Stream.concat(getExceptionsFromAppenders(name),
                findLongestMapEntryInExceptions(defaultExceptions, name).stream()))
                .map(Map.Entry::getValue);
    }

    private Stream<? extends Map.Entry<String, Logger.LogLevel>> getExceptionsFromAppenders(final String name) {
        return getByNameWithMaxLength(
                appenders.stream()
                        .map(Appender::getExceptions)
                        .flatMap(a -> findLongestMapEntryInExceptions(a, name).stream())).stream();
    }

    private Logger.LogLevel getMinLevel(final DisLogManager disLogManager) {
        return appenders.stream()
                .map(Appender::getLogLevel)
                .min(Comparator.comparingInt(Logger.LogLevel::getWeight))
                .orElseGet(disLogManager::getDefaultLevel);
    }



}