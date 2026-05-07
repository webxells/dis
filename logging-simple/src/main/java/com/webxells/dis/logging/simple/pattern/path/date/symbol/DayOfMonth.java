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
package com.webxells.dis.logging.simple.pattern.path.date.symbol;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class DayOfMonth extends SimpleSymbol {
    public static class Builder implements DateSymbol.Builder<DayOfMonth> {
        @Override
        public boolean isResponsible(final String chunk) {
            return "dd".equals(chunk);
        }

        @Override
        public DayOfMonth build(final LocalDateTime now) {
            return new DayOfMonth(now, null);
        }
    }

    private static final long DAY_MILLIS =  86400000L;

    private List<Integer> daysOfMonth;

    public DayOfMonth(final LocalDateTime now, final Object as) {
        this(now);
    }

    public DayOfMonth(final LocalDateTime now) {
        super(now, 2);
    }

    @Override
    protected String getStartFormat(final int length) {
        return "dd";
    }

    @Override
    protected long stripOverage(final long amount, final int offset) {
        long result = amount + offset;
        for (final int current : daysOfMonth) {
            if (1 > result - current) {
                return result;
            }
            result -= current;
        }
        return result;
    }

    @Override
    protected long getSomeCacheGuideline() {
        return DAY_MILLIS;
    }

    @Override
    protected LocalDateTime getDeltaDate(final int delta) {
        if (null == daysOfMonth) {
             daysOfMonth = new ArrayList<>();
        }
        int year = now.getYear();
        int month = now.getMonthValue();
        int day = now.getDayOfMonth() + delta;
        int currentRun = 0;
        do {
            final int lengthMonth = YearMonth.of(year, month).lengthOfMonth();
            if (currentRun++ >= daysOfMonth.size()) {
                daysOfMonth.add(lengthMonth);
            }
            if (day <= lengthMonth) {
                break;
            }
            day -= lengthMonth;
            if (12 == month) {
                year++;
                month -= 12;
            }
            month++;
        } while (true);
        return LocalDateTime.of(year, month, day, 0, 0);
    }
}