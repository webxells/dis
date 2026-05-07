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

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.error.InvalidApi;
import java.io.IOException;

public abstract class ValidatingReader<T> extends TypeAdapter<T> {

    @Override
    public void write(final JsonWriter out, final T value) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public T read(final JsonReader in) throws IOException {
        final T result = readClass(in);
        if (result instanceof DisApi) {
            try {
                ((DisApi) result).validate();
            } catch (final InvalidApi e) {
                throw new IOException("Invalid class build", e);
            }
        }
        return result;
    }

    abstract T readClass(final JsonReader in) throws IOException;
}