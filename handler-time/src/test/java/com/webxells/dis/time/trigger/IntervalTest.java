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
package com.webxells.dis.time.trigger;

import com.webxells.dis.test.cases.SimpleTestCase;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.TimeZone;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class IntervalTest extends SimpleTestCase {
    @Test
    void test() {
        long interval = random(1L);
        ChronoUnit unit = ChronoUnit.MONTHS;
        try(MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            IntervalConfig config = new IntervalConfig();
            config.setInterval(interval);
            config.setUnit(unit);
            config.setRunOnStart(true);

            LocalDateTime mock1 = mock(LocalDateTime.class);
            LocalDateTime mock2 = mock(LocalDateTime.class);
            when(mock2.isAfter(same(mock1)))
                    .thenReturn(true);

            LocalDateTime mock3 = mock(LocalDateTime.class);
            LocalDateTime mock4 = mock(LocalDateTime.class);
            LocalDateTime mock5 = mock(LocalDateTime.class);
            LocalDateTime mock6 = mock(LocalDateTime.class);

            when(mock3.plus(eq(interval), eq(unit)))
                    .thenReturn(mock4);
            when(mock3.isAfter(same(mock4)))
                    .thenReturn(false);
            when(mock5.isAfter(same(mock4)))
                    .thenReturn(false);
            when(mock6.isAfter(same(mock4)))
                    .thenReturn(true);

            localDateTime.when(() -> LocalDateTime.now(TimeZone.getDefault().toZoneId()))
                    .thenReturn(mock1) // create::          (nextRun) run on start
                    .thenReturn(mock2) // shouldTrigger1::  (now) time to trigger
                    .thenReturn(mock3) // shouldTrigger2::  (now)
                    .thenReturn(mock4) // shouldTrigger2::  (nextRun) nope
                    .thenReturn(mock5) // shouldTrigger3::  (now) nope
                    .thenReturn(mock6); //shouldTrigger4::  (now) yep

            Interval fixture = new Interval(config);
            assertTrue(fixture.shouldTrigger());
            assertFalse(fixture.shouldTrigger());
            assertFalse(fixture.shouldTrigger());
            assertFalse(fixture.shouldTrigger());
            assertTrue(fixture.shouldTrigger());
        }
    }

}