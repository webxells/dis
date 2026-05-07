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
package org.slf4j.event;

public enum Level {
    ERROR(EventConstants.ERROR_INT),
    WARN(EventConstants.WARN_INT),
    INFO(EventConstants.INFO_INT),
    DEBUG(EventConstants.DEBUG_INT),
    TRACE(EventConstants.TRACE_INT);

    private final int value;

    Level(int value) {
        this.value = value;
    }

    public int toInt() {
        return value;
    }

    public static Level intToLevel(int levelInt) {
        return switch (levelInt) {
            case EventConstants.TRACE_INT -> TRACE;
            case EventConstants.DEBUG_INT -> DEBUG;
            case EventConstants.INFO_INT -> INFO;
            case EventConstants.WARN_INT -> WARN;
            case EventConstants.ERROR_INT -> ERROR;
            default -> throw new IllegalArgumentException("Unknown level");
        };
    }

    public String toString() {
        return name();
    }

}