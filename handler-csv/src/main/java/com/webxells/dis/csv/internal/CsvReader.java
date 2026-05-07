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

import com.webxells.dis.api.Logger;
import com.webxells.dis.csv.CsvInputConfig;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CsvReader implements Closeable {
    private static final Logger LOGGER = LoggerProxyFactory.logger(CsvReader.class);

    private final BufferedReader stream;
    private final RowReader rowReader;
    private final boolean ignoreEmptyLines;

    private CsvRow peeked;
    private boolean endReached;

    public CsvReader(final InputStream receive, final CsvInputConfig config) throws IOException {
        stream = new BufferedReader(new InputStreamReader(receive, config.getCharset()));
        rowReader = new RowReader(config);
        ignoreEmptyLines = config.isIgnoringEmptyLines();
        skipLines(config.getSkipLines());
        if (config.isFirstLineAsHeaders()) {
            rowReader.setHeader(readCsvRow());
        }
    }

    private void skipLines(int skipLines) throws IOException {
        while (0 < skipLines--) {
            readNextCsvRow();
        }
    }

    public boolean hasNext() throws IOException {
        return null != peek();
    }

    public CsvRow peek() throws IOException {
        if (null == peeked) {
            peeked = readCsvRow();
        }
        return peeked;
    }

    public CsvRow read() throws IOException {
        if (null != peeked) {
            final CsvRow row = peeked;
            peeked = null;
            return row;
        }
        return readCsvRow();
    }

    private CsvRow readCsvRow() throws IOException {
        CsvRow result;
        do {
            result = readNextCsvRow();
        } while (null != result && ignoreEmptyLines && result.hasNoContent());
        return result;
    }

    private CsvRow readNextCsvRow() throws IOException {
        if (endReached) {
            return null;
        }
        final RowReader.RowBuilder rowBuilder = rowReader.newRow();
        int rawRead;
        while (-1 < (rawRead = stream.read())) {
            if (rowBuilder.parse((char) rawRead)) {
                break;
            }
        }
        if (-1 == rawRead) {
            LOGGER.t("End of csv stream reached");
            endReached = true;
        }
        return rowBuilder.getRow(endReached);
    }

    @Override
    public void close() throws IOException {
        stream.close();
        endReached = true;
    }
}