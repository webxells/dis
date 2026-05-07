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

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.time.trigger.unit.DayOfMonth;
import com.webxells.dis.time.trigger.unit.DayOfWeek;
import com.webxells.dis.time.trigger.unit.Hour;
import com.webxells.dis.time.trigger.unit.Minute;
import com.webxells.dis.time.trigger.unit.Month;
import com.webxells.dis.time.trigger.unit.Second;
import com.webxells.dis.time.trigger.unit.TimeUnit;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Time extends SimpleTimeTrigger<TimeConfig> {
    private static final Logger LOGGER = LoggerProxyFactory.logger(Time.class);

    private final TimeUnit instant;

    public Time(final TimeConfig config) {
        super(config);
        instant = calcTime(config.getInstant(), config);
    }

    @Override
    public void validate() throws InvalidApi {
        if (null == instant) {
            throw new InvalidApi("time cannot be null");
        }
    }

    @Override
    protected LocalDateTime calculateNextRun(final LocalDateTime now) {
        final LocalDateTime localDateTime = instant.calcNextRun(now);
        LOGGER.debug("calculated next run time: %s", localDateTime);
        return localDateTime;
    }

    private TimeUnit calcTime(final TimeUnit instant, final TimeConfig config) {
        if (null != instant) {
            return instant;
        }
        final String time = config.getTime();
        if (null != time) {
            return calcTime(time);
        }
        final String cron = config.getCron();
        if (null != cron) {
            return calcCron(cron);
        }
        return null;
    }

    private TimeUnit calcCron(final String cron) {
        final String[] parts = cron.split("\\s+");
        if (5 == parts.length) {
            final Integer minute = getIntWildcard(parts[0]);
            final Integer hour = getIntWildcard(parts[1]);
            final Integer dayOfMonth = getIntWildcard(parts[2]);
            final Integer month = getIntWildcard(parts[3]);
            final Integer dayOfWeek = getIntWildcard(parts[4]);
            if (range(minute, 0, 59) && range(hour, 0, 23) &&
                    range(dayOfMonth, 1, 31) && range(month, 1, 12) &&
                    range(dayOfWeek, 0, 6)) {
                final List<TimeUnit> morePrecise = new ArrayList<>();
                if (666 != minute) {
                    morePrecise.add(new Minute(minute, List.of()));
                }
                if (666 != hour) {
                    morePrecise.add(new Hour(hour, List.of()));
                }
                if (666 != dayOfMonth) {
                    morePrecise.add(new DayOfMonth(dayOfMonth, List.of()));
                }
                if (666 != month) {
                    morePrecise.add(new Month(month, List.of()));
                }
                if (666 != dayOfWeek) {
                    morePrecise.add(new DayOfWeek(dayOfMonth, List.of()));
                }
                return new Second(0, morePrecise);
            }
        }
        return null;
    }

    private boolean range(final Integer value, final int min, final int max) {
        return notNull(value) && (min <= value && max >= value) || 666 == value;
    }

    private TimeUnit calcTime(final String hour) {
        final int hourMinute = hour.indexOf(":");
        final int minuteSecond = hour.lastIndexOf(":");
        final Integer hourValue = getInt(hour.substring(0, hourMinute));
        final Integer minute = getInt(hour.substring(hourMinute + 1, hourMinute == minuteSecond ? hour.length() : minuteSecond));
        final Integer second = hourMinute < minuteSecond ?
                getInt(hour.substring(minuteSecond + 1)) : Integer.valueOf(0);
        if (notNull(hourValue, minute, second)) {
            return new Hour(hourValue, List.of(new Minute(minute, List.of(new Second(second, List.of())))));
        }
        return null;
    }

    private boolean notNull(final Object... objects) {
        return Arrays.stream(objects).allMatch(Objects::nonNull);
    }

    private Integer getIntWildcard(final String part) {
        if (part.equals("*")) {
            return 666;
        }
        return getInt(part);
    }

    private Integer getInt(final String substring) {
        if (substring.chars().allMatch(Character::isDigit)) {
            return Integer.parseInt(substring);
        }
        return null;
    }
}