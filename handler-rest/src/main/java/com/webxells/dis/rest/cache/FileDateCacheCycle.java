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
package com.webxells.dis.rest.cache;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.error.InvalidApi;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

public class FileDateCacheCycle implements ConfigurableByType {
    private int amount;
    private ChronoUnit unit;

    @Override
    public void validate() throws InvalidApi {
        if (null == unit || 1 > amount) {
            throw new InvalidApi("Invalid cache cycle");
        }
    }

    boolean revalidate(final Path path) throws IOException {
        final LocalDateTime lastModifiedTime = LocalDateTime.ofInstant(Files.getLastModifiedTime(path).toInstant(), ZoneId.systemDefault());
        final LocalDateTime acceptable = LocalDateTime.now()
                .minus(Duration.of(amount, unit));
        return lastModifiedTime.isBefore(acceptable);
    }

    public void setAmount(final int amount) {
        this.amount = amount;
    }

    public void setUnit(final ChronoUnit unit) {
        this.unit = unit;
    }
}