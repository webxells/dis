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
package com.webxells.dis.time.validator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.validator.SingleCallForAllValuesValidator;
import com.webxells.dis.time.intern.SimpleTimeApi;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Description("Stops config for a specified duration")
public class Pause implements SingleCallForAllValuesValidator {
    @Required
    private long duration;
    @Default("SECONDS")
    private ChronoUnit unit = SimpleTimeApi.DEFAULT_UNIT;

    @Override
    public void validate() throws InvalidApi {
        if (duration < 1) {
            throw new InvalidApi("Won't do this - set duration greater 0");
        }
    }

    @Override
    public boolean validate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        try {
            Thread.sleep(calculateDuration());
        } catch (final InterruptedException e) {
            throw new RuntimeException("Nightmare!", e);
        }
        return true;
    }

    private long calculateDuration() {
        return Duration.of(duration, unit).toMillis();
    }

    public void setDuration(final long duration) {
        this.duration = duration;
    }

    public void setUnit(final ChronoUnit unit) {
        this.unit = unit;
    }
}