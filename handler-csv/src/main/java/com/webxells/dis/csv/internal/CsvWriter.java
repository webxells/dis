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

import com.webxells.dis.csv.CsvConfig;
import com.webxells.dis.csv.CsvOutputConfig;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

public class CsvWriter implements Closeable {
    record Entry(String path, String value) {}
    public class RowWriter {
        private final Queue<Entry> values = new LinkedList<>();
        private boolean fieldSeparatorRequired;

        public void write(final String path, final String value) {
            values.add(new Entry(path, value));
        }

        public void flush() throws IOException {
            handleHeaders();
            for (int i = Optional.ofNullable(ownHeaders)
                    .map(List::size)
                    .orElse(values.size()); 0 < i--;) {
                writeValue(Optional.ofNullable(values.poll())
                        .map(Entry::value)
                        .orElse(null));
            }
            lineEnding();
            writer.flush();
        }

        private void handleHeaders() throws IOException {
            if (!headersWritten) {
                writeHeaders(Optional.ofNullable(ownHeaders)
                        .orElseGet(() -> values.stream().map(Entry::path).toList()));
                headersWritten = true;
                fieldSeparatorRequired = false;
            }
        }

        private void writeValue(final String value) throws IOException {
            if (fieldSeparatorRequired) {
                writer.write(fieldSeparator);
            } else {
                fieldSeparatorRequired = true;
            }
            if (null == value) {
                return;
            }
            final boolean valueSeparatorRequired =
                    value.isEmpty() ||
                            value.chars().anyMatch(a -> '\n' == a || '\r' == a || fieldSeparator == a || valueSeparator == a);
            if (valueSeparatorRequired) {
                writer.write(valueSeparator);
            }
            for (final char current : value.toCharArray()) {
                if (valueSeparator == current) {
                    writer.write(valueSeparator);
                }
                writer.write(current);
            }
            if (valueSeparatorRequired) {
                writer.write(valueSeparator);
            }
        }

        private void writeHeaders(final List<String> headers) throws IOException {
            for (final String a : headers) {
                writeValue(a);
            }
            lineEnding();
        }

        private void lineEnding() throws IOException {
            writer.write(lineEnding.chars());
        }
    }

    private final BufferedWriter writer;
    private final List<String> ownHeaders;
    private final char fieldSeparator;
    private final char valueSeparator;
    private final CsvConfig.NewLine lineEnding;

    private boolean headersWritten;

    public CsvWriter(final OutputStream send, final CsvOutputConfig config) {
        writer = new BufferedWriter(new OutputStreamWriter(send, config.getCharset()));
        ownHeaders = config.getHeaderFields();
        headersWritten = !config.isFirstLineAsHeaders();
        fieldSeparator = config.getFieldSeparator();
        valueSeparator = config.getValueSeparator();
        lineEnding = config.getLineEnding();
    }

    public RowWriter newRow() {
        return new RowWriter();
    }

    @Override
    public void close() throws IOException {
        if (null != writer) {
            writer.close();
        }
    }
}