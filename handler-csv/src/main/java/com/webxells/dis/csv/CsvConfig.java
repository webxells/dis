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
package com.webxells.dis.csv;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

import com.webxells.dis.api.config.description.Required;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Optional;

public abstract class CsvConfig {
    public enum NewLine {
        @Description("\\n") UNIX("\n"), @Description("\\r\\n") WINDOWS("\r\n"),
        @Description("\\r") OLD_MAC("\r"), @Description("\\n\\r") RISC_OS("\n\r");
        private final String chars;
        private final int length;

        public static Optional<NewLine> endWithEnding(final String string) {
            return Arrays.stream(NewLine.values())
                    .filter(a -> string.endsWith(a.chars))
                    .findFirst();

        }

        NewLine(final String chars) {
            this.chars = chars;
            length = chars.length();
        }

        public String chars() {
            return chars;
        }

        public int length() {
            return length;
        }
    }

    @Description("Reference of handler")
    @Required
    protected String name;

    @Description("Specifies character to separate fields")
    @Default(";")
    protected char fieldSeparator = ';';
    @Description("Specifies character to bracket values in field")
    @Default("\"")
    protected char valueSeparator = '"';
    @Description("Use Rfc4180 csv standard - " +
            "settings regarding rfc4180 definitions are ignored - e.g. field or line separator")
    @Default("No Rfc4180 is used")
    private boolean rfc4180 = false;
    @Description("Ignores this amount of first lines. If provided with firstLineAsHeaders, row after skipping will be used for headers.")
    @Default("0")
    private int skipLines = 0;
    @Description("Handles first line as headers - otherwise mapping path are column indexes starting by 0")
    @Default("true")
    private boolean firstLineAsHeaders = true;
    @Description("Specifies line ending")
    @Default("UNIX")
    private NewLine lineEnding = NewLine.UNIX;
    protected Charset charset = StandardCharsets.UTF_8;

    public String getName() {
        return name;
    }

    public char getFieldSeparator() {
        return rfc4180 ? ',' : fieldSeparator;
    }

    public void setFieldSeparator(final char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public char getValueSeparator() {
        return rfc4180 ? '"' : valueSeparator;
    }

    public void setValueSeparator(final char valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public Charset getCharset() {
        return charset;
    }

    @Default("UTF-8")
    public void setCharset(final String charset) {
        this.charset = Charset.forName(charset);
    }

    public void setName(final String name) {
        this.name = name;
    }

    public int getSkipLines() {
        return rfc4180 ? 0 : skipLines;
    }

    public void setSkipLines(final int skipLines) {
        this.skipLines = skipLines;
    }

    public boolean isFirstLineAsHeaders() {
        return firstLineAsHeaders;
    }

    public void setFirstLineAsHeaders(final boolean firstLineAsHeaders) {
        this.firstLineAsHeaders = firstLineAsHeaders;
    }

    public void setCharset(final Charset charset) {
        this.charset = charset;
    }

    public NewLine getLineEnding() {
        return rfc4180 ? NewLine.WINDOWS : lineEnding;
    }

    public void setLineEnding(final NewLine lineEnding) {
        this.lineEnding = lineEnding;
    }

    public boolean isRfc4180() {
        return rfc4180;
    }

    public void setRfc4180(final boolean rfc4180) {
        this.rfc4180 = rfc4180;
    }
}