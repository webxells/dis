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
package com.webxells.dis.time.trigger.unit;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

public abstract class MorePreciseUnit implements TimeUnit {
    protected List<TimeUnit> morePrecise;

    public abstract LocalDateTime calcNextRunForCurrent(final LocalDateTime now);

    public abstract LocalDateTime increase(final LocalDateTime now);

    @Override
    public LocalDateTime calcNextRun(final LocalDateTime now) {
        LocalDateTime result = calcWholeNextRun(now);
        LocalDateTime next = now;
        while (now.isAfter(result)) {
            next = increase(next);
            result = calcWholeNextRun(next);
        }
        return result;
    }

    private LocalDateTime calcWholeNextRun(final LocalDateTime now) {
        final AtomicReference<LocalDateTime> result = new AtomicReference<>(calcNextRunForCurrent(now));
        Optional.ofNullable(morePrecise).stream()
                .flatMap(Collection::stream)
                .forEach(a -> result.set(a.calcNextRun(result.get())));
        return result.get();
    }

    public void setMorePrecise(final List<TimeUnit> morePrecise) {
        this.morePrecise = morePrecise;
    }
}