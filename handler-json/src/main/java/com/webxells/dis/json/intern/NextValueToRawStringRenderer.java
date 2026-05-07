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
package com.webxells.dis.json.intern;

import com.google.gson.stream.JsonReader;
import com.webxells.dis.json.JsonException;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class NextValueToRawStringRenderer {
    private final JsonReader jsonReader;
    private final StringBuilder result = new StringBuilder();
    /**
     * LinkedList of Map.Entry of levelIsAnArray and commaIsRequired
     */
    private final LinkedList<Map.Entry<AtomicBoolean, AtomicBoolean>> depthRequiresComma = new LinkedList<>();

    public static String parse(final JsonReader jsonReader) throws JsonException, IOException {
        final NextValueToRawStringRenderer instance = new NextValueToRawStringRenderer(jsonReader);
        return instance.parseNextValue();
    }

    private NextValueToRawStringRenderer(final JsonReader jsonReader) {
        this.jsonReader = jsonReader;
    }

    private String parseNextValue() throws JsonException, IOException {
        while (0 < depthRequiresComma.size() || 0 == result.length()) {
            comma();
            switch (jsonReader.peek()) {
                case BEGIN_ARRAY:
                    beginArray();
                    break;
                case END_ARRAY:
                    endArray();
                    break;
                case BEGIN_OBJECT:
                    beginObject();
                    break;
                case END_OBJECT:
                    endObject();
                    break;
                case NAME:
                    addName();
                    break;
                case STRING:
                    addStringValue();
                    break;
                case NUMBER:
                    result.append(getNumber(jsonReader.nextDouble()));
                    break;
                case BOOLEAN:
                    result.append(jsonReader.nextBoolean());
                    break;
                case NULL:
                    nextNull();
                    break;
                case END_DOCUMENT:
                    throw new JsonException("unexpected document end");
            }
        }
        return result.toString();
    }

    private void comma() throws IOException {
        if (depthRequiresComma.isEmpty()) {
            return;
        }
        final Map.Entry<AtomicBoolean, AtomicBoolean> last = depthRequiresComma.getLast();
        switch (jsonReader.peek()) {
            case BEGIN_ARRAY:
            case BEGIN_OBJECT:
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                if (last.getKey().get()) {
                    writeComma(last.getValue());
                }
                break;
            case NAME:
                if (!last.getKey().get()) {
                    writeComma(last.getValue());
                }
        }
    }

    private void writeComma(final AtomicBoolean shouldWriteComma) {
        if (shouldWriteComma.getAndSet(true)) {
            result.append(",");
        }
    }

    private void nextNull() throws IOException {
        jsonReader.nextNull();
        result.append("null");
    }

    private void addStringValue() throws IOException {
        result.append("\"");
        result.append(escapeQuotes(jsonReader.nextString()));
        result.append("\"");
    }

    private void addName() throws IOException {
        result.append("\"");
        result.append(escapeQuotes(jsonReader.nextName()));
        result.append("\":");
    }

    private void endObject() throws IOException {
        jsonReader.endObject();
        result.append("}");
        depthRequiresComma.removeLast();
    }

    private void beginObject() throws IOException {
        jsonReader.beginObject();
        result.append("{");
        depthRequiresComma.add(Map.entry(new AtomicBoolean(false), new AtomicBoolean(false)));
    }

    private void endArray() throws IOException {
        jsonReader.endArray();
        result.append("]");
        depthRequiresComma.removeLast();
    }

    private void beginArray() throws IOException {
        jsonReader.beginArray();
        result.append("[");
        depthRequiresComma.add(Map.entry(new AtomicBoolean(true), new AtomicBoolean(false)));
    }


    private String getNumber(final double numericValue) {
        if (Math.floor(numericValue) == numericValue) {
            return String.valueOf((long) numericValue);
        }
        return String.valueOf(numericValue);
    }

    private String escapeQuotes(final String json) {
        return json.replace("\"", "\\\"");
    }

}