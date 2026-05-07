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
package com.webxells.dis.time.trigger.unit;

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidApi;
import java.time.LocalDateTime;
import java.util.List;

@Description("Triggers every minute at given second; times format [0-59]")
public class Second extends IntUnit {

    public Second() { }

    public Second(final Integer value, final List<TimeUnit> morePrecise) {
        super(value, morePrecise);
    }

    @Override
    public void validate() throws InvalidApi {
        if (59 < value || 0 > value) {
            throw new InvalidApi("Invalid second");
        }
    }

    @Override
    public LocalDateTime calcNextRunForCurrent(final LocalDateTime now) {
        return now.withSecond(value);
    }

    @Override
    public LocalDateTime increase(final LocalDateTime now) {
        return now.plusMinutes(1);
    }
}