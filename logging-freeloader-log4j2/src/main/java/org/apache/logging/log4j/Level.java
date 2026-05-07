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

import java.util.Locale;
import java.util.Optional;
import org.apache.logging.log4j.spi.StandardLevel;

public class Level {
    public static final Level OFF = new Level("OFF");
    public static final Level FATAL = new Level("FATAL");
    public static final Level ERROR = new Level("ERROR");
    public static final Level WARN = new Level("WARN");
    public static final Level INFO = new Level("INFO");
    public static final Level DEBUG = new Level("DEBUG");
    public static final Level TRACE = new Level("TRACE");
    public static final Level ALL = new Level("ALL");
    public static final String CATEGORY = "Level";


    private final String name;
    private final int intLevel;
    private final StandardLevel standardLevel;

    private Level(final String name) {
        this.name = name;
        standardLevel = StandardLevel.valueOf(name);
        intLevel = standardLevel.intLevel();
    }

    public int intLevel() {
        return this.intLevel;
    }

    public StandardLevel getStandardLevel() {
        return this.standardLevel;
    }

    public boolean isInRange(final Level minLevel, final Level maxLevel) {
        return this.intLevel >= minLevel.intLevel && this.intLevel <= maxLevel.intLevel;
    }

    public boolean isLessSpecificThan(final Level level) {
        return this.intLevel >= level.intLevel;
    }

    public boolean isMoreSpecificThan(final Level level) {
        return this.intLevel <= level.intLevel;
    }

    public Level clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    public int compareTo(final Level other) {
        return Integer.compare(this.intLevel, other.intLevel);
    }

    public Class<Level> getDeclaringClass() {
        return Level.class;
    }

    public int hashCode() {
        return this.name.hashCode();
    }

    public String name() {
        return this.name;
    }

    public String toString() {
        return this.name;
    }

    public static Level forName(final String name) {
        return switch (name) {
            case "OFF" -> OFF;
            case "FATAL" -> FATAL;
            case "ERROR" -> ERROR;
            case "WARN" -> WARN;
            case "INFO" -> INFO;
            case "DEBUG" -> DEBUG;
            case "TRACE" -> TRACE;
            case "ALL" -> ALL;
            default -> throw new IllegalStateException("Unexpected value: " + name);
        };
    }

    public static Level getLevel(final String name) {
        return forName(name);
    }

    public static Level toLevel(final String level) {
        return forName(level);
    }

    public static Level toLevel(final String name, final Level defaultLevel) {
        return Optional.ofNullable(name)
                .map(Level::forName)
                .orElse(defaultLevel);
    }

    private static String toUpperCase(final String name) {
        return name.toUpperCase(Locale.ENGLISH);
    }

    public static Level[] values() {
        return new Level[] { OFF, FATAL, ERROR, WARN, INFO, DEBUG, TRACE, ALL} ;
    }

    public static Level valueOf(final String name) {
        return forName(name);
    }

    public static <T extends Enum<T>> T valueOf(final Class<T> enumType, final String name) {
        return Enum.valueOf(enumType, name);
    }

    protected Object readResolve() {
        return valueOf(this.name);
    }

}