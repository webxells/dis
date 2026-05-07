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

import com.webxells.dis.api.error.InvalidApi;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PauseTest {

    @Test
    void test() throws InvalidApi {
        Pause fixture = new Pause();
        assertThrows(InvalidApi.class, fixture::validate);
        fixture.setDuration(2);
        fixture.validate();

        test(fixture, 2000);

        fixture.setUnit(ChronoUnit.MILLIS);
        fixture.setDuration(650);

        test(fixture, 650);
    }

    private void test(final Pause fixture, final int minDuration) {
        long start = System.currentTimeMillis();
        fixture.validate(null, null);

        assertTrue((System.currentTimeMillis() - start) >= minDuration);

    }

}