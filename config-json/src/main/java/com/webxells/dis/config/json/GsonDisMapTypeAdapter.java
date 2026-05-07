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
package com.webxells.dis.config.json;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.webxells.dis.boot.ConfigurationMapping;
import com.webxells.dis.config.json.intern.SourceMapping;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class GsonDisMapTypeAdapter<T> extends ValidatingReader<T> {
    private final Gson gson;
    private final TypeToken<?> token;

    GsonDisMapTypeAdapter(final Gson gson, final TypeToken<?> token) {
        this.gson = gson;
        this.token = token;
    }

    @Override
    T readClass(final JsonReader in) throws IOException {
        final Map<String, Object> result = new LinkedHashMap<>();
        assert JsonToken.BEGIN_OBJECT.equals(in.peek()) : "Invalid type for MapTypeAdapter: not an object";
        in.beginObject();
        while(!JsonToken.END_OBJECT.equals(in.peek())) {
            final String key = in.nextName();
            if (SourceMapping.getMappingKey().equals(key)) {
                in.skipValue();
                continue;
            }
            result.put(key, gson.fromJson(getValue(in), token.getType()));
        }
        in.endObject();
        return (T) result;
    }

    private String getValue(final JsonReader in) throws IOException {
        return gson.toJson(getNextValue(in));
    }

    private Object getNextObject(final JsonReader in) throws IOException {
        assert JsonToken.BEGIN_OBJECT.equals(in.peek()) : "Invalid value-type for MapTypeAdapter: not an object";
        final Map<String, Object> result = new LinkedHashMap<>();
        in.beginObject();
        while(!JsonToken.END_OBJECT.equals(in.peek())) {
            if (JsonToken.NULL == in.peek()) {
                in.nextNull();
                continue;
            }
            String name = in.nextName();
            result.put(name, getNextValue(in));
        }
        in.endObject();
        return result;
    }

    private Object getNextValue(final JsonReader in) throws IOException {
        switch(in.peek()) {
            case BOOLEAN:
                return in.nextBoolean();
            case STRING:
            case NUMBER:
                return in.nextString();
            case NULL:
                in.nextNull();
                return null;
            case BEGIN_OBJECT:
                return getNextObject(in);
            case BEGIN_ARRAY:
                final List<Object> result = new LinkedList<>();
                in.beginArray();
                while (!JsonToken.END_ARRAY.equals(in.peek())) {
                    result.add(getNextValue(in));
                }
                in.endArray();
                return result;
        }
        throw new IOException("Invalid Json - needed value but got: ".concat(String.valueOf(in.peek())));
    }

}