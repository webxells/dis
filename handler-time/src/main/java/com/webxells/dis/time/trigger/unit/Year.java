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
import java.time.LocalDate;
import java.time.LocalDateTime;

@Description("Triggers at given year; times format [1-12]")
public class Year extends IntUnit {

    @Override
    public void validate() throws InvalidApi {
        super.validate();
        if (LocalDate.now().getYear() > value) {
            throw new InvalidApi("year cannot be greater than current year");
        }
    }

    @Override
    public LocalDateTime calcNextRunForCurrent(final LocalDateTime now) {
        assertNowOrLater(now.getYear(), value);
        return now.withYear(value);
    }

    @Override
    public LocalDateTime increase(final LocalDateTime now) {
        throw new CalculationException("Impossible to increase time context of a year");
    }

}