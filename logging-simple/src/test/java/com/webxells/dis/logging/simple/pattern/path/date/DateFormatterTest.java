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
package com.webxells.dis.logging.simple.pattern.path.date;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DateFormatterTest {
    private static final ZoneOffset ZONE_OFFSET = ZoneId.systemDefault()
            .getRules()
            .getOffset(LocalDateTime.now());

    @Test
    void test() {
        DateFormatter fixture = new DateFormatter("yyyy-MM-dd HH:mm:ss,SSS");
        LocalDateTime now = fixture.getNow();
        String formatedNowValue = fixture.formatDiff(toTimestamp(now));
        assertEquals(now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")), formatedNowValue);
        assertEquals(23, formatedNowValue.length());
        assertTrue(formatedNowValue.matches("^\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2},\\d{3}"));
        assertEquals(formatedNowValue, fixture.formatDiff(toTimestamp(now.plusNanos(5))));
        final LocalDateTime futureDate = now.plusDays(353).plusSeconds(253245).minusNanos(843449583);
        assertEquals(futureDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss,SSS")), fixture.formatDiff(toTimestamp(futureDate)));
    }

    protected long toTimestamp(final LocalDateTime dateTime) {
        return dateTime.atZone(ZONE_OFFSET)
                .toInstant().toEpochMilli();
    }

}