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
package com.webxells.dis.csv.internal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class CsvRow {
    public class ColumnSorter<T> {
        public interface Matcher<R> {
            int match(List<R> parts, List<CsvCell> columns) throws IOException;
        }
        public interface UnknownColumnHandler {
            int handle(CsvCell column) throws IOException;
        }

        private Matcher<T> matcher;
        private UnknownColumnHandler unknownHandler;

        public int match(final Map<String, List<T>> objectMapping) throws IOException {
            int result = 0;
            if (null != matcher) {
                for (final Map.Entry<String, List<T>> current : objectMapping.entrySet()) {
                    result += matcher.match(current.getValue(), findByName(current.getKey()));
                }
            }
            if (null != unknownHandler) {
                for (final CsvCell column : rawValues) {
                    if (!objectMapping.containsKey(column.header())) {
                        result+= unknownHandler.handle(column);
                    }
                }
            }
            return result;
        }

        private List<CsvCell> findByName(final String key) {
            return rawValues.stream()
                    .filter(a -> key.equals(a.header()))
                    .toList();
        }

        public ColumnSorter<T> matcher(final Matcher<T> matcher) {
            this.matcher = matcher;
            return this;
        }

        public ColumnSorter<T> unknown(final UnknownColumnHandler unknownHandler) {
            this.unknownHandler = unknownHandler;
            return this;
        }

    }

    private final CsvRow header;
    private final List<CsvCell> rawValues =  new ArrayList<>();
    private final String source;

    CsvRow(final CsvRow header, final String source) {
        this.header = header;
        this.source = source;
    }

    public CsvRow getHeader() {
        return null == header ? createIndexHeader() : header;
    }

    private CsvRow createIndexHeader() {
        final AtomicInteger current = new AtomicInteger();
        final CsvRow result = new CsvRow(null, source);
        rawValues.forEach(a -> result.append(String.valueOf(current.get()), current.getAndIncrement()));
        return result;
    }

    public CsvCell getByIndex(final int index) {
        return rawValues.get(index);
    }

    public Stream<CsvCell> stream() {
        return rawValues.stream();
    }

    public void append(final String value, final int index) {
        final CsvCell headerCell = Optional.ofNullable(header)
                        .map(a -> a.getByIndex(index))
                        .orElseGet(() -> new CsvCell(source, null, String.valueOf(index)));
        rawValues.add(new CsvCell(headerCell.source(), headerCell.value(), value));
    }

    public boolean hasNoContent() {
        return rawValues.isEmpty();
    }

    public <T> ColumnSorter<T> sorter() {
        return new ColumnSorter<>();
    }

    public void clean() {
        rawValues.forEach(CsvCell::clean);
    }

    public int length() {
        return rawValues.size();
    }
}