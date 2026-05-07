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
package com.webxells.dis.rest;

import com.webxells.dis.api.BinaryData;
import com.webxells.dis.base.binary.RawData;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;

public class MultiPartInputStream extends InputStream {
    private final static String INTERNATIONAL_LINE_ENDING = "\r\n";
    private final LinkedHashMap<String, BinaryData> parts = new LinkedHashMap<>();

    private BinaryData currentData;
    private InputStream currentStream;
    private String currentName;
    private final String boundary;
    private boolean writeMetaData;

    public MultiPartInputStream(final String boundary) {
        this.boundary = boundary;
    }

    public void addData(final String name, final InputStream stream) {
        addData(name, new RawData(stream));
    }

    public void addData(final String name, final BinaryData binaryData) {
        parts.put(name, binaryData);
    }

    public LinkedHashMap<String, BinaryData> getParts() {
        return parts;
    }

    @Override
    public int read() throws IOException {
        return readRefreshedStream();
    }

    public int size() {
        return parts.size();
    }

    String createSeparator() {
        return String.format("--%s", boundary);
    }

    private int readRefreshedStream() throws IOException {
        if (null != currentStream) {
            final int result = currentStream.read();
            if (result > -1) {
                return result;
            }
        }
        refreshCurrent();
        return null == currentStream ? -1 : currentStream.read();
    }

    private void refreshCurrent() throws IOException {
        if (null == currentStream) {
            if (parts.isEmpty()) {
                return;
            }
            writeMetaData(String.format("%s%s", createSeparator(), INTERNATIONAL_LINE_ENDING));
            return;
        }
        currentStream.close();
        if (!writeMetaData && null != currentData) {
            writeMetaData(String.format("%s%s%s", INTERNATIONAL_LINE_ENDING, createSeparator(), parts.isEmpty() ? "--" :
                    INTERNATIONAL_LINE_ENDING));
            currentData = null;
            return;
        } else if (writeMetaData && null == currentData) {
            writeNextFileDescription();
            return;
        }
        writeMetaData = false;
        currentStream = Objects.requireNonNull(currentData).getContent();
    }

    private void writeNextFileDescription() {
        if (parts.isEmpty()) {
            currentStream = null;
        } else {
            popNextData();
            writeFileMetaData();
        }
    }

    private void writeFileMetaData() {
        final String content = generateDataMetaDataString();
        writeMetaData(content);
    }

    private String generateDataMetaDataString() {
        return String.format("Content-Disposition:form-data;name=\"%s\"", currentName) +
                Optional.ofNullable(currentData.getName())
                        .map(a -> String.format(";filename=\"%s\"", a)).orElse("") +
                String.format("%sContent-Type:%s%s%s", INTERNATIONAL_LINE_ENDING, currentData.getMimeType(),
                        INTERNATIONAL_LINE_ENDING, INTERNATIONAL_LINE_ENDING);
    }

    private void writeMetaData(final String value) {
        currentStream = new ByteArrayInputStream(value.getBytes());
        writeMetaData = true;
    }

    private void popNextData() {
        currentName = parts.keySet().iterator().next();
        final BinaryData result = parts.get(currentName);
        parts.remove(currentName);
        currentData = result;
    }
}