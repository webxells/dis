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

public class Year extends SimpleSymbol {
    public static class Builder implements DateSymbol.Builder<Year> {
        private String chunk;

        @Override
        public boolean isResponsible(final String chunk) {
            this.chunk = chunk;
            final int length = chunk.length();
            final char firstChar = chunk.charAt(0);
            return ('Y' == firstChar || 'y' == firstChar) &&
                    (2 == length || 4 == length);
        }

        @Override
        public Year build(final LocalDateTime now) {
            return new Year(now, chunk.length());
        }
    }

    private static final long YEAR_MILLIS = 30000000000L;

    public Year(final LocalDateTime now, final int length) {
        super(now, length);
    }

    @Override
    protected String getStartFormat(final int length) {
        return "y".repeat(length);
    }

    @Override
    protected long stripOverage(final long year, final int offset) {
        return year + offset;
    }

    @Override
    protected long getSomeCacheGuideline() {
        return YEAR_MILLIS;
    }

    @Override
    protected LocalDateTime getDeltaDate(final int delta) {
        return LocalDateTime.of(now.getYear() + delta, 1, 1, 0, 0);
    }
}