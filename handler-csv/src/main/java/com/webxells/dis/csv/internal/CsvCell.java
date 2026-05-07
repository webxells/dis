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

public final class CsvCell {
    private final String source;
    private final String header;
    private String value;

    public CsvCell(final String source, final String header, final String value) {
        this.source = source;
        this.header = header;
        this.value = value;
    }

    public boolean partPresent() {
        return null != value;
    }

    public void clean() {
        if (null != value) {
            value = value.replaceAll("\\00", "");
        }
    }

    public String source() {
        return source;
    }

    public String header() {
        return header;
    }

    public String value() {
        return value;
    }
}