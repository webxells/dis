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
package com.webxells.dis.csv.discover.intern;

import com.webxells.dis.csv.CsvConfig.NewLine;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class CsvConfigurationGuesser {

    private final List<CsvPattern> patterns = List.of(new CsvPattern(List.of(new Value(), new FieldSeparator())));
    private final int maxRowsToBeExplored;

    private String line;
    private int skipLines;
    private NewLine lineEnding;
    private Character valueSeparator;
    private char fieldSeparator;

    public CsvConfigurationGuesser(final File file, final int maxRowsToBeExplored) throws IOException {
        this.maxRowsToBeExplored = maxRowsToBeExplored;
        try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))) {
            guessFileEnding(bufferedInputStream);
            analyzeLines(bufferedInputStream);
        }
    }

    public int getSkipLines() {
        return skipLines;
    }

    public NewLine getLineEnding() {
        return lineEnding;
    }

    public Character getValueSeparator() {
        return valueSeparator;
    }

    public char getFieldSeparator() {
        return fieldSeparator;
    }

    private void analyzeLines(final BufferedInputStream bufferedInputStream) throws IOException {
        final Map<CsvPatternResult, AtomicInteger> allPatterns = new HashMap<>();
        final Map<CsvPatternResult, Integer> firstAppearance = new HashMap<>();
        for (int currentLine = 0; currentLine < maxRowsToBeExplored || 0 == maxRowsToBeExplored; currentLine++) {
            if (0 < currentLine) {
                nextLine(bufferedInputStream);
            }
            if (null == line) {
                break;
            }
            final CsvPatternResult currentLinePattern = findCurrentPattern();
            if (null != currentLinePattern) {
                allPatterns.computeIfAbsent(currentLinePattern, a -> new AtomicInteger()).incrementAndGet();
                if (!firstAppearance.containsKey(currentLinePattern)) {
                    firstAppearance.put(currentLinePattern, currentLine);
                }
            }
        }
        final CsvPatternResult mostUsedPattern = getMostUsedPattern(allPatterns);
        skipLines = firstAppearance.get(mostUsedPattern);
        setPatterns(mostUsedPattern);
    }

    private CsvPatternResult getMostUsedPattern(final Map<CsvPatternResult, AtomicInteger> allPatterns) throws IOException {
        return allPatterns.entrySet().stream()
                .max(Comparator.comparingInt(a -> a.getValue().get()))
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IOException("No pattern found"));
    }

    private void setPatterns(final CsvPatternResult resultPattern) {
        if (null == resultPattern) {
            throw new IllegalStateException("Could not detect a csv pattern");
        }
        Optional.ofNullable(resultPattern.getValueSeparator())
                .ifPresent(a -> valueSeparator = a);
        fieldSeparator = resultPattern.getFieldSeparator();
    }

    private void nextLine(final BufferedInputStream bufferedInputStream) throws IOException {
        line = removeLineEnding(readTillNewLine(bufferedInputStream));
    }

    private String removeLineEnding(final String rawLine) {
        return null == rawLine ? null :
                null == lineEnding ? rawLine : rawLine.substring(0, rawLine.length() - lineEnding.length());
    }

    private CsvPatternResult findCurrentPattern() {
        return patterns.stream()
                .filter(a -> a.match(line))
                .findFirst()
                .map(this::createResult)
                    .orElse(null);
    }

    private CsvPatternResult createResult(final CsvPattern pattern) {
        return new CsvPatternResult(pattern.getFieldSeparator(), pattern.getValueSeparator());
    }

    private String readTillNewLine(final BufferedInputStream bufferedInputStream) throws IOException {
        final StringBuilder result = new StringBuilder();
        char last;
        int lastRaw;
        do {
            lastRaw = bufferedInputStream.read();
            if (-1 == lastRaw) {
                break;
            }
            last = (char) lastRaw;
            result.append(last);
        } while (NewLine.endWithEnding(result.toString()).isEmpty());
        return result.isEmpty() ? null : result.toString();
    }

    private void guessFileEnding(final BufferedInputStream bufferedInputStream) throws IOException {
        final String line = readTillNewLine(bufferedInputStream);
        if (null == line) {
            throw new IllegalStateException("nothing to read");
        }
        lineEnding = NewLine.endWithEnding(line)
                .orElse(null);
        this.line = removeLineEnding(line);
    }
}