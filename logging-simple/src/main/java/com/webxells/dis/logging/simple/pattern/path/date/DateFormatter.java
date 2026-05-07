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

import com.webxells.dis.logging.simple.pattern.path.date.symbol.DateSymbol;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.DayOfMonth;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.FractionOfSecond;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.Hour;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.Minute;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.NumericMonth;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.RawText;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.Second;
import com.webxells.dis.logging.simple.pattern.path.date.symbol.Year;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DateFormatter {
    private static final List<DateSymbol.Builder<?>> DATE_SYMBOL_BUILDERS = List.of(
            new FractionOfSecond.Builder(),
            new Second.Builder(),
            new Minute.Builder(),
            new Hour.Builder(),
            new DayOfMonth.Builder(),
            new NumericMonth.Builder(),
            new Year.Builder(),
            new RawText.Builder() //has to be last
    );

    private final LocalDateTime now = LocalDateTime.now();
    private final List<DateSymbol> format;

    public DateFormatter(final String format) {
        this.format = formatString(format);
    }

    LocalDateTime getNow() {
        return now;
    }

    public String formatDiff(final long date) {
        return format.stream()
                .map(a -> a.getDiffFormat(date))
                .collect(Collectors.joining());
    }

    private List<DateSymbol> formatString(final String string) {
        final List<DateSymbol> result = new ArrayList<>();
        for (final String chunk : getFormatChunks(string)) {
            final DateSymbol.Builder<?> builder = getSymbolBuilder(chunk);
            result.add(builder.build(now));
        }
        return result;
    }

    private DateSymbol.Builder<?> getSymbolBuilder(final String chunk) {
        return DATE_SYMBOL_BUILDERS.stream()
                .filter(a -> a.isResponsible(chunk))
                .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown symbol for chunk: " + chunk));
    }

    private List<String> getFormatChunks(final String string) {
        final List<String> result = new ArrayList<>();
        final StringBuilder currentResult = new StringBuilder();
        for(final byte currentByte : string.getBytes(StandardCharsets.UTF_8)) {
            final String currentChar = String.valueOf((char) currentByte);
            if(!currentResult.isEmpty() && -1 == currentResult.indexOf(currentChar)) {
                result.add(currentResult.toString());
                currentResult.setLength(0);
            }
            currentResult.append(currentChar);
        }
        if (0 < currentResult.length()) {
            result.add(currentResult.toString());
        }
        return result;
    }
}