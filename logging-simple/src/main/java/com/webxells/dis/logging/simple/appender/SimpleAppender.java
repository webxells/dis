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
import com.webxells.dis.logging.simple.PatternPath;
import com.webxells.dis.logging.simple.internal.ConcurrentLoggingDirector;
import com.webxells.dis.logging.simple.internal.Ring;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public abstract class SimpleAppender implements Appender {
    private final Ring<Event> lastEvents = new Ring<>();

    public Logger.LogLevel level;
    public String pattern;
    public Map<String, Logger.LogLevel> exceptions;
    public int linesBefore;
    public int linesAfter;
    public int linesBeforeAndAfter;
    public String separator;

    private Integer linesAfterToDo;

    protected PatternPath patternPath;

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public Map<String, Logger.LogLevel> getExceptions() {
        return exceptions;
    }

    @Override
    public Logger.LogLevel getLogLevel() {
        return level;
    }

    public void setLevel(final Logger.LogLevel level) {
        this.level = level;
    }

    public void setPattern(final String pattern) {
        this.pattern = pattern;
    }

    public void setExceptions(final Map<String, Logger.LogLevel> exceptions) {
        this.exceptions = exceptions;
    }


    @Override
    public void fillDefaults(final DisLogManager disLogManager) {
        if (null == level) {
            level = disLogManager.getDefaultLevel();
        }
        if (null == pattern) {
            pattern = disLogManager.getDefaultPattern();
        }
        if (null == exceptions) {
            exceptions = disLogManager.getDefaultExceptions();
        }
    }

    protected void assertAllSet() {
        if (null == level || null == pattern) {
            throw new RuntimeException("level and pattern are mandatory fields");
        }
        if (null == patternPath) {
            patternPath = PatternPath.compile(pattern);
        }
        if (linesBeforeAndAfter > 0) {
            linesAfter = linesBeforeAndAfter;
            linesBefore = linesBeforeAndAfter;
        }
        lastEvents.setCapacity(linesBefore);
    }

    protected boolean isNotable(final Event event) {
        final Logger.LogLevel level = getLevelForEvent(event);
        final boolean result = event.getLevel().getWeight() >= level.getWeight();
        writeBeforeAndAfter(result, event);
        return result;
    }

    private Logger.LogLevel getLevelForEvent(final Event event) {
        return Optional.ofNullable(exceptions)
                .map(a -> ConcurrentLoggingDirector.findLongestNameInExceptions(a, event.getName()))
                .orElse(level);
    }

    private void writeBeforeAndAfter(final boolean result, final Event event) {
        writeBefore(result, event);
        writeAfter(result, event);
        writeSeparator(result);
        if (result && linesAfter > 0) {
            linesAfterToDo = linesAfter;
        }
    }

    private void writeSeparator(final boolean result) {
        if ((null != linesAfterToDo && 0 == linesAfterToDo)) {
            if (null != separator) {
                writeRaw(separator);
            }
            linesAfterToDo = null;
        }
    }

    private void writeAfter(final boolean result, final Event event) {
        if (!result && null != linesAfterToDo) {
            if (0 < linesAfterToDo) {
                forceWrite(event);
                linesAfterToDo--;
            }
        }
    }

    private void writeBefore(final boolean result, final Event event) {
        if (linesBefore > 0) {
            if (result) {
                lastEvents.get().stream()
                        .filter(Objects::nonNull)
                        .forEach(this::forceWrite);
                lastEvents.clear();
            } else if (null == linesAfterToDo) {
                lastEvents.add(event);
            }
        }
    }
}