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
import java.io.IOException;

class RowReader {
    class RowBuilder {
        private final CsvRow row = new CsvRow(header, source);
        private final StringBuilder currentValue = new StringBuilder();
        private boolean insideString;
        private int columnLength;
        private int rowLength;
        private int countValueSeparator;
        private int currentColumnIndex;

        /**
         * @param current current char to append
         * @return boolean if parsing should stop
         */
        boolean parse(final char current) {
            columnLength++;
            rowLength++;
            if (valueSeparator == current) {
                countValueSeparator++;
                return false;
            } else if (0 < countValueSeparator){
                handleCountValueSeparator();
                countValueSeparator = 0;
            }
            if (fieldSeparator == current && !insideString) {
                createColumn(false);
                return false;
            } else {
                currentValue.append(current);
            }
            final int possibleLineEndingIndex = currentValue.length() - lineEndingLength;
            if (!insideString && -1 < possibleLineEndingIndex &&
                    currentValue.substring(possibleLineEndingIndex).equals(lineEnding)) {
                currentValue.delete(possibleLineEndingIndex, possibleLineEndingIndex + lineEndingLength);
                return true;
            }
            return false;
        }

        CsvRow getRow(final boolean eofReached) throws IOException {
            if (eofReached) {
                if (0 < countValueSeparator) {
                    handleCountValueSeparator();
                }
                if (insideString) {
                    throw new IOException("end of file inside string");
                }
            }
            if (lineEndingLength < rowLength || (eofReached && 0 < rowLength) || 0 < currentValue.length()) {
                createColumn(eofReached);
            }
            return row;
        }

        private void handleCountValueSeparator() {
            if (2 < countValueSeparator) {
                appendValueSeparatorsToValue();
            }
            if (1 == countValueSeparator % 2) {
                insideString = !insideString;
            }

        }

        private void appendValueSeparatorsToValue() {
            currentValue.append(String.valueOf(valueSeparator)
                    .repeat(countValueSeparator / 2));
        }

        private void createColumn(final boolean eofReached) {
            row.append(1 < columnLength || 0 < currentValue.length() ? currentValue.toString() : null, currentColumnIndex++);
            currentValue.setLength(0);
            columnLength = 0;
        }
    }

    private final char fieldSeparator;
    private final String lineEnding;
    private final int lineEndingLength;
    private final char valueSeparator;
    private final String source;
    private CsvRow header;

    public RowReader(final CsvConfig config) {
        fieldSeparator = config.getFieldSeparator();
        lineEnding = config.getLineEnding().chars();
        lineEndingLength = config.getLineEnding().length();
        valueSeparator = config.getValueSeparator();
        source = config.getName();
    }

    public void setHeader(final CsvRow csvRow) {
        header = csvRow;
    }

    RowBuilder newRow() {
        return new RowBuilder();
    }
}