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

public class NumericMonth extends SimpleSymbol {
    public static class Builder implements DateSymbol.Builder<NumericMonth> {
        private String chunk;

        @Override
        public boolean isResponsible(final String chunk) {
            this.chunk = chunk;
            final int length = chunk.length();
            return chunk.startsWith("M") &&
                    (1 == length || 2 == length);
        }

        @Override
        public NumericMonth build(final LocalDateTime now) {
            return new NumericMonth(now, chunk.length());
        }
    }

    private static final long MONTH_MILLIS =  2000000000L;

    public NumericMonth(final LocalDateTime now, final int length) {
        super(now, length);
    }

    @Override
    protected String getStartFormat(final int length) {
        return "MM";
    }

    @Override
    protected long stripOverage(final long amount, final int offset) {
        final long result = (amount + offset) % 12;
        return result == 0 ? 12 : result;
    }

    @Override
    protected long getSomeCacheGuideline() {
        return MONTH_MILLIS;
    }

    @Override
    protected LocalDateTime getDeltaDate(final int delta) {
        int year = now.getYear();
        int newMonth = now.getMonthValue() + delta;
        while (12 < newMonth) {
            newMonth = newMonth - 12;
            year = year + 1;
        }
        return LocalDateTime.of(year, newMonth, 1, 0, 0);
    }
}