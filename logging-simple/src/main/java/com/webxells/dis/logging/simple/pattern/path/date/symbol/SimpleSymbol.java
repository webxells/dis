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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

abstract class SimpleSymbol implements DateSymbol {
    static final ZoneOffset ZONE_OFFSET = ZoneId.systemDefault()
            .getRules()
            .getOffset(LocalDateTime.now());

    protected final long timestamp;
    protected final LocalDateTime now;
    protected final long startFormatedValue;
    protected final int length;
    protected final List<Long> tillNext = new ArrayList<>();

    public SimpleSymbol(final LocalDateTime now, final int length) {
        this.now = now;
        this.timestamp = toTimestamp(now);
        createFirstNextCaches();
        this.length = length;
        final String start = getCurrent(now, getStartFormat(length));
        if (start.chars().anyMatch(a -> !Character.isDigit(a))) {
            throw new IllegalArgumentException("Invalid year provided");
        }
        this.startFormatedValue = Long.parseLong(start);
    }

    protected abstract String getStartFormat(final int length);

    @Override
    public String getDiffFormat(final long date) {
        final long diff = date - timestamp;
        final int cacheRequired = ((int) (diff / getSomeCacheGuideline())) + 2;
        if (cacheRequired > tillNext.size()) {
            for (int i = tillNext.size(); i < cacheRequired; i++) {
                createNextCache(i + 1);
            }
        }
        for (int i = 0, m = tillNext.size(); i < m; i++) {
            if (date < tillNext.get(i)) {
                return format(stripOverage(startFormatedValue, i), length);
            }
        }
        throw new RuntimeException("Too few cache calculated");
    }

    static long toTimestamp(final LocalDateTime dateTime) {
        return dateTime.atZone(ZONE_OFFSET)
                .toInstant().toEpochMilli();
    }

    static String format(final long value, final int length) {
        final String result = String.valueOf(value);
        final int resultLength = result.length();
        if (resultLength == length ) {
            return result;
        }
        if (resultLength < length) {
            return String.format("%s%s", "0".repeat(length - resultLength), result);
        }
        return result.substring(resultLength - length);
    }

    protected abstract long stripOverage(final long amount, final int offset);

    protected abstract long getSomeCacheGuideline();

    protected abstract LocalDateTime getDeltaDate(final int delta);

    protected void createNextCache(final int delta) {
        tillNext.add(toTimestamp(getDeltaDate(delta)));
    }


    protected String getCurrent(final LocalDateTime date, final String format) {
        return date.format(DateTimeFormatter.ofPattern(format));
    }

    private void createFirstNextCaches() {
        createNextCache(1);
        createNextCache(2);
    }
}