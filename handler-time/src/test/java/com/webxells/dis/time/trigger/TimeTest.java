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

import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.test.cases.SimpleTestCase;
import com.webxells.dis.time.trigger.unit.TimeUnit;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

class TimeTest extends SimpleTestCase {
    private static final ZoneId DEFAULT_ZONE = TimeZone.getDefault().toZoneId();

    @Test
    void testCron() throws InvalidApi {
        TimeConfig config = new TimeConfig();
        int hour = randomMax(23);
        int minute = randomMax(59);
        config.setCron(String.format("%d   %d  * *  *", minute, hour));
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            LocalDateTime mock1 = mock(LocalDateTime.class);
            LocalDateTime mock2 = mock(LocalDateTime.class);
            LocalDateTime mock3 = mock(LocalDateTime.class);
            LocalDateTime mock4 = mock(LocalDateTime.class);
            LocalDateTime mock5 = mock(LocalDateTime.class);

            when(mock1.withSecond(eq(0)))
                    .thenReturn(mock2);

            when(mock2.withMinute(eq(minute)))
                    .thenReturn(mock3);

            when(mock3.withHour(eq(hour)))
                    .thenReturn(mock4);

            when(mock4.isAfter(same(mock1)))
                    .thenReturn(false);

            when(mock5.isAfter(same(mock4)))
                    .thenReturn(true);

            localDateTime.when(() -> LocalDateTime.now(DEFAULT_ZONE))
                    .thenReturn(mock1)
                    .thenReturn(mock5);

            Time fixture = new Time(config);
            fixture.validate();
            assertFalse(fixture.shouldTrigger());
            assertTrue(fixture.shouldTrigger());
        }
    }

    @Test
    void testTimeWithSeconds() throws InvalidApi {
        TimeConfig config = new TimeConfig();
        int hour = randomMax(23);
        int minute = randomMax(59);
        int seconds = randomMax(59);
        config.setTime(String.format("%s:%d:%d", 10 > hour ? "0".concat(String.valueOf(hour)) : hour, minute, seconds));
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            LocalDateTime mock1 = mock(LocalDateTime.class);
            LocalDateTime mock2 = mock(LocalDateTime.class);
            LocalDateTime mock3 = mock(LocalDateTime.class);
            LocalDateTime mock4 = mock(LocalDateTime.class);
            LocalDateTime mock5 = mock(LocalDateTime.class);

            when(mock1.withHour(eq(hour)))
                    .thenReturn(mock2);

            when(mock2.withMinute(eq(minute)))
                    .thenReturn(mock3);

            when(mock3.withSecond(eq(seconds)))
                    .thenReturn(mock4);

            when(mock4.isAfter(same(mock1)))
                    .thenReturn(false);

            when(mock5.isAfter(same(mock4)))
                    .thenReturn(true);

            localDateTime.when(() -> LocalDateTime.now(DEFAULT_ZONE))
                    .thenReturn(mock1)
                    .thenReturn(mock5);

            Time fixture = new Time(config);
            fixture.validate();
            assertFalse(fixture.shouldTrigger());
            assertTrue(fixture.shouldTrigger());
        }
    }

    @Test
    void testTime() throws InvalidApi {
        TimeConfig config = new TimeConfig();
        int hour = randomMax(23);
        int minute = randomMax(59);
        config.setTime(String.format("%s:%d", 10 > hour ? "0".concat(String.valueOf(hour)) : hour, minute));
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            LocalDateTime mock1 = mock(LocalDateTime.class);
            LocalDateTime mock2 = mock(LocalDateTime.class);
            LocalDateTime mock3 = mock(LocalDateTime.class);
            LocalDateTime mock4 = mock(LocalDateTime.class);
            LocalDateTime mock5 = mock(LocalDateTime.class);

            when(mock1.withHour(eq(hour)))
                    .thenReturn(mock2);

            when(mock2.withMinute(eq(minute)))
                    .thenReturn(mock3);

            when(mock3.withSecond(eq(0)))
                    .thenReturn(mock4);

            when(mock4.isAfter(same(mock1)))
                    .thenReturn(false);

            when(mock5.isAfter(same(mock4)))
                    .thenReturn(true);

            localDateTime.when(() -> LocalDateTime.now(DEFAULT_ZONE))
                    .thenReturn(mock1)
                    .thenReturn(mock5);

            Time fixture = new Time(config);
            fixture.validate();
            assertFalse(fixture.shouldTrigger());
            assertTrue(fixture.shouldTrigger());
        }
    }

    @Test
    void test() throws InvalidApi {
        TimeConfig config = new TimeConfig();
        config.setRunOnStart(true);
        TimeUnit timeUnit = mock(TimeUnit.class);
        try (MockedStatic<LocalDateTime> localDateTime = Mockito.mockStatic(LocalDateTime.class)) {
            LocalDateTime mock1 = mock(LocalDateTime.class);
            LocalDateTime mock2 = mock(LocalDateTime.class);
            when(mock2.isAfter(same(mock1)))
                    .thenReturn(true);

            LocalDateTime mock3 = mock(LocalDateTime.class);
            LocalDateTime mock4 = mock(LocalDateTime.class);
            LocalDateTime mock5 = mock(LocalDateTime.class);

            when(timeUnit.calcNextRun(same(mock3)))
                    .thenReturn(mock4);
            when(mock3.isAfter(same(mock4)))
                    .thenReturn(false);
            when(mock5.isAfter(same(mock4)))
                    .thenReturn(true);

            localDateTime.when(() -> LocalDateTime.now(DEFAULT_ZONE))
                    .thenReturn(mock1)
                    .thenReturn(mock2)
                    .thenReturn(mock3)
                    .thenReturn(mock5);

            config.setInstant(timeUnit);
            Time fixture = new Time(config);
            fixture.validate();
            assertTrue(fixture.shouldTrigger());
            assertFalse(fixture.shouldTrigger());
            assertTrue(fixture.shouldTrigger());
        }
    }

}