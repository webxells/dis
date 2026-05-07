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
package com.webxells.dis.time.intern;

import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;

public abstract class SimpleTimeApi implements DisApi {
    public static final DateTimeFormatter DEFAULT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    public static final TimeZone DEFAULT_ZONE = TimeZone.getDefault();
    public static final ChronoUnit DEFAULT_UNIT = ChronoUnit.SECONDS;
    @Required
    @Default("yyyy-MM-ddTHH:mm:ss")
    protected DateTimeFormatter format = SimpleTimeApi.DEFAULT_FORMAT;
    @Required
    @Default("Zone of this machine")
    protected TimeZone zone = SimpleTimeApi.DEFAULT_ZONE;

    @Override
    public void validate() throws InvalidApi {
        if (null == zone || null == format) {
            throw new InvalidApi("format and zone are required");
        }
    }

    public void setZone(final String zone) {
        this.zone = TimeZone.getTimeZone(zone);
    }

    public void setFormat(final String format) {
        this.format = DateTimeFormatter.ofPattern(format);
    }

    public DateTimeFormatter getFormat() {
        return format;
    }

    public TimeZone getZone() {
            return zone;
        }
}