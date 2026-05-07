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

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

class GsonDisCollectionTypeAdapter<T> extends ValidatingReader<T> {
    private final TypeAdapter<?> childAdapter;
    private final boolean isSet;

    GsonDisCollectionTypeAdapter(final TypeAdapter<?> childAdapter, final boolean isSet) {
        this.childAdapter = Objects.requireNonNull(childAdapter);
        this.isSet = isSet;
    }

    @Override
    T readClass(final JsonReader in) throws IOException {
        final Collection<Object> result = isSet ? new HashSet<>() : new LinkedList<>();
        assertNextIsArray(in);
        in.beginArray();
        while(!JsonToken.END_ARRAY.equals(in.peek())) {
            result.add(childAdapter.read(in));
        }
        in.endArray();
        return (T) result;
    }

    private void assertNextIsArray(final JsonReader in) throws IOException {
        if (!JsonToken.BEGIN_ARRAY.equals(in.peek())) {
            throw new IOException("Invalid type for ListTypeAdapter: not an array");
        }
    }

}