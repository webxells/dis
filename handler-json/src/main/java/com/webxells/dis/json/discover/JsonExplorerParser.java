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
package com.webxells.dis.json.discover;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import java.io.IOException;
import java.io.Reader;
import java.util.Optional;

public class JsonExplorerParser {
    enum NecessaryToken {
        VALUE, END, OTHER
    }
    private final JsonReader reader;
    private JsonValue next;
    public JsonExplorerParser(final Reader reader) {
        this.reader = new JsonReader(reader);
        this.reader.setLenient(true);
    }

    public boolean hasNext() throws IOException {
        peek();
        return !(next == null && peekReader() == NecessaryToken.END);
    }

    public Optional<JsonValue> next() throws IOException {
        if (null == next) {
            peek();
        }
        return peekedValue();
    }

    NecessaryToken peekReader() throws IOException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
            case END_ARRAY:
            case BEGIN_OBJECT:
            case END_OBJECT:
            case NAME:
                return NecessaryToken.OTHER;
            case STRING:
            case NUMBER:
            case BOOLEAN:
            case NULL:
                return NecessaryToken.VALUE;
            case END_DOCUMENT:
                return NecessaryToken.END;
        }
        throw new IllegalStateException("unknown json token");
    }

    private void peek() throws IOException {
        if (null != next) {
            return;
        }
        while (NecessaryToken.OTHER == peekReader()) {
            skipUnnecessaryJson();
        }
        if (NecessaryToken.VALUE == peekReader()) {
            next = new JsonValue(readJsonValue(), reader.getPath());
        }
    }

    private String readJsonValue() throws IOException {
        if (JsonToken.NULL == reader.peek()) {
            reader.nextNull();
            return null;
        }
        return reader.nextString();
    }

    private Optional<JsonValue> peekedValue() {
        final JsonValue result = next;
        next = null;
        return Optional.ofNullable(result);
    }

    private void skipUnnecessaryJson() throws IOException {
        switch (reader.peek()) {
            case BEGIN_ARRAY:
                reader.beginArray();
                break;
            case END_ARRAY:
                reader.endArray();
                break;
            case BEGIN_OBJECT:
                reader.beginObject();
                break;
            case END_OBJECT:
                reader.endObject();
                break;
            case NAME:
                reader.nextName();
                break;
        }
    }
}