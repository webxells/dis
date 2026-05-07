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

import com.webxells.dis.csv.discover.ColumnExplorer;
import com.webxells.dis.csv.internal.CsvCell;
import com.webxells.dis.csv.internal.CsvReader;
import com.webxells.dis.csv.internal.CsvRow;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class HeaderExplorer {
    private final CsvRow possibleTextHeaders;
    private final List<AtomicLong> sumLength;
    private final List<AtomicInteger> nonTextTypes;
    private final int headerLength;
    private final CsvRow resultHeaders;
    private int datasetCount;


    public HeaderExplorer(final CsvReader csvReader) throws IOException {
        possibleTextHeaders = ColumnExplorer.clean(csvReader.read());
        headerLength = possibleTextHeaders.length();
        sumLength = createList(AtomicLong::new);
        nonTextTypes = createList(AtomicInteger::new);
        iterateThroughContent(csvReader);
        resultHeaders = generateHeaders();
    }

    private <T> List<T> createList(final Supplier<T> createInstance) {
        final ArrayList<T> result = new ArrayList<>(headerLength);
        IntStream.range(0, headerLength)
                .forEach(a -> result.add(createInstance.get()));
        return result;
    }

    private void iterateThroughContent(final CsvReader csvReader) throws IOException {
        CsvRow current;
        while (null != (current = ColumnExplorer.clean(csvReader.read()))) {
            datasetCount++;
            for (int i = 0, m = Math.min(current.length(), headerLength); i < m; i++) {
                final CsvCell cell = current.getByIndex(i);
                if (null != cell) {
                    sumLength.get(i).addAndGet(
                            Optional.ofNullable(cell.value())
                                    .map(String::length)
                                    .orElse(0));
                    if (!isTextContent(cell.value())) {
                        nonTextTypes.get(i).incrementAndGet();
                    }
                }
            }
        }
    }

    private boolean isTextContent(final String content) {
        final String trimmedContent = trim(content);
        return !(null == trimmedContent || trimmedContent.isEmpty() ||
                trimmedContent.chars().allMatch(a -> Character.isDigit(a) || '.' == a || ',' == a));
    }

    private String trim(final String content) {
        return null == content ? null : content.replaceAll("\\s+", "");
    }

    public CsvRow generateHeaders() {
        if (mostOfHeadersAreText()) {
            if (typesOfTextHeadersMismatching() || lengthOfHeadersAndContentMismatch()) {
                return possibleTextHeaders;
            }
        }
        return possibleTextHeaders.getHeader();
    }

    private boolean lengthOfHeadersAndContentMismatch() {
        int headersWithLengthDifferentInAverageContent = 0;
        for (int i = 0; i < headerLength; i++) {
            final int headerContentLength = possibleTextHeaders.getByIndex(i).value().length();
            if ((double) sumLength.get(i).get() / datasetCount > headerContentLength + (((double) headerContentLength / 100) * 30)) {
                headersWithLengthDifferentInAverageContent++;
            }
        }
        return mostHeaders() <= headersWithLengthDifferentInAverageContent;
    }

    private boolean typesOfTextHeadersMismatching() {
        int textHeadersWithNontextContentCount = 0;
        for (int i = 0; i < possibleTextHeaders.length(); i++) {
            if (isTextContent(possibleTextHeaders.getByIndex(i).value())) {
                final double averageNonTexts = averagePercent(nonTextTypes.get(i).get());
                if (averageNonTexts > 95) {
                    return true;
                }
                if (averageNonTexts > 70) {
                    textHeadersWithNontextContentCount++;
                }
            }
        }
        return mostHeaders() <= textHeadersWithNontextContentCount;
    }

    private double averagePercent(final int count) {
         return (double) count / ((double) datasetCount / 100);
    }

    private boolean mostOfHeadersAreText() {
        return mostHeaders() <=
                possibleTextHeaders.stream()
                        .map(CsvCell::value)
                        .filter(this::isTextContent)
                        .count();
    }

    private double mostHeaders() {
        final double length = headerLength;
        return Math.floor(headerLength < 5 ? length / 2 : length - (length / 40));
    }

    public boolean hasHeaders() {
        return resultHeaders == possibleTextHeaders;
    }

    public CsvRow getHeaders() {
        return resultHeaders;
    }
}