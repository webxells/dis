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

import com.webxells.dis.api.Logger;
import java.time.LocalDateTime;

public class Event {
    private final long date = System.currentTimeMillis();
    private final Thread thread;
    private final Logger.LogLevel level;
    private final String name;
    private final String msg;
    private final Throwable e;

    public Event(final Thread thread, final Logger.LogLevel level, final String name, final String msg) {
        this(thread, level, name, msg, null);
    }

    public Event(final Thread thread, final Logger.LogLevel level, final String name, final String msg, final Throwable e) {
        this.thread = thread;
        this.level = level;
        this.name = name;
        this.msg = msg;
        this.e = e;
    }

    public Thread getThread() {
        return thread;
    }

    public Logger.LogLevel getLevel() {
        return level;
    }

    public String getMsg() {
        return msg;
    }

    public Throwable getE() {
        return e;
    }

    public long getDate() {
        return date;
    }

    public String getName() {
        return name;
    }
}