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
package com.webxells.dis.test;

import com.webxells.dis.api.LogManager;
import com.webxells.dis.api.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.Assertions;

public class TestLogManager implements LogManager {
    public class TestLogger implements Logger {
        public record Event(LogLevel level, String message, Throwable throwable) {
            @Override
            public boolean equals(final Object o) {
                if (this == o) return true;
                if (!(o instanceof final Event event)) return false;
                return level == event.level &&
                        Objects.equals(message, event.message) &&
                        (
                                (null == throwable && null == event.throwable) ||
                                (null != throwable && null != event.throwable &&
                                        throwable.getClass() == event.throwable.getClass() &&
                                        Objects.equals(throwable.getMessage(), event.throwable.getMessage())));
            }
        }

        private final List<Event> events = Collections.synchronizedList(new ArrayList<>());
        private LogLevel level;

        public List<Event> getEvents() {
            return Collections.unmodifiableList(events);
        }

        public void setLevel(final LogLevel level) {
            this.level = level;
        }

        public void assertEventWasFired(final LogLevel level, final String msg, final Throwable e) {
            Assertions.assertTrue(events.stream()
                    .anyMatch(a -> a.equals(new Event(level, msg, e))));
        }

        public void assertEventWasFired(final LogLevel level, final String msg) {
            assertEventWasFired(level, msg, null);
        }

        public void assertEventWasFired(final LogLevel level, final Throwable e) {
            assertEventWasFired(level, null, e);
        }

        @Override
        public boolean has(final LogLevel otherLevel) {
            return level != null && level.getWeight() <= otherLevel.getWeight();
        }

        @Override
        public void holdAll() {
            holdAll = true;
        }

        @Override
        public void freezeAll() {
            freezeAll = true;
        }

        @Override
        public boolean allOnHold() {
            return holdAll;
        }

        @Override
        public void resumeAll() {
            freezeAll = false;
            holdAll = false;
        }

        @Override
        public LogLevel getLevel() {
            return LogLevel.TRACE;
        }

        @Override
        public void fatal(final String msg) {
            events.add(new Event(LogLevel.FATAL, msg, null));
        }

        @Override
        public void fatal(final String msg, final Throwable e) {
            events.add(new Event(LogLevel.FATAL, msg, e));
        }

        @Override
        public void fatal(final Throwable e) {
            events.add(new Event(LogLevel.FATAL, null, e));
        }

        @Override
        public void error(final String msg) {
            events.add(new Event(LogLevel.ERROR, msg, null));
        }

        @Override
        public void error(final String msg, final Throwable e) {
            events.add(new Event(LogLevel.ERROR, msg, e));
        }

        @Override
        public void error(final Throwable e) {
            events.add(new Event(LogLevel.ERROR, null, e));
        }

        @Override
        public void warn(final String msg) {
            events.add(new Event(LogLevel.WARN, msg, null));
        }

        @Override
        public void warn(final String msg, final Throwable e) {
            events.add(new Event(LogLevel.WARN, msg, e));
        }

        @Override
        public void warn(final Throwable e) {
            events.add(new Event(LogLevel.WARN,null, e));
        }

        @Override
        public void info(final String msg) {
            events.add(new Event(LogLevel.INFO, msg, null));
        }

        @Override
        public void info(final String msg, final Throwable e) {
            events.add(new Event(LogLevel.INFO, msg, e));
        }

        @Override
        public void info(final Throwable e) {
            events.add(new Event(LogLevel.INFO,null, e));
        }

        @Override
        public void debug(final String msg) {
            events.add(new Event(LogLevel.DEBUG, msg, null));
        }

        @Override
        public void debug(final String msg, final Throwable e) {
            events.add(new Event(LogLevel.DEBUG, msg, e));
        }

        @Override
        public void debug(final Throwable e) {
            events.add(new Event(LogLevel.DEBUG, null, e));
        }

        @Override
        public void trace(final String msg) {
            events.add(new Event(LogLevel.TRACE, msg, null));
        }

        public void clear() {
            events.clear();
        }
    }

    private final Map<String, TestLogger> instances = new ConcurrentHashMap<>();

    private boolean holdAll;
    private boolean freezeAll;

    @Override
    public TestLogger get(final String name) {
        return instances.computeIfAbsent(name, a -> new TestLogger());
    }

    @Override
    public TestLogger get(final Class<?> clazz) {
        return get(clazz.getName());
    }

    public boolean isHoldAll() {
        return holdAll;
    }

    public boolean isFreezeAll() {
        return freezeAll;
    }

    public void clear() {
        instances.forEach((a, b) -> b.clear());
    }
}