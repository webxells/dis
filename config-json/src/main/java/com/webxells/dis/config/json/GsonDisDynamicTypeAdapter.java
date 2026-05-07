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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.config.json.intern.ClassInitiator;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class GsonDisDynamicTypeAdapter<T> extends ValidatingReader<T> {
    protected final Gson gson;
    protected final GsonDisAdapterFactory factory;
    private final String typeMapping;

    public GsonDisDynamicTypeAdapter(final Gson gson, final GsonDisAdapterFactory factory, final String typeMapping) {
        this.gson = gson;
        this.factory = factory;
        this.typeMapping = typeMapping;
    }

    @Override
    T readClass(final JsonReader in) throws IOException {
        final JsonElement current = gson.fromJson(in, JsonElement.class);
        if (current.isJsonObject()) {
            return readTypedObject((JsonObject) current);
        }
        throw new IOException("Invalid call of Adapter::read");
    }

    protected T readTypedObject(final JsonObject object) throws IOException {
        final String type = ClassInitiator.getTypeByJsonObject(object, typeMapping);
        return gson.fromJson(object, getGenericTypeByConfigProcessor(DisConfigApi.class, type));
    }

    private <U> Type getGenericTypeByConfigProcessor(final Class<U> lookingFor, final String classAsString) throws IOException {
        try {
            final Class<?> clazz = ClassInitiator.loadClass(classAsString);
            if (lookingFor.isAssignableFrom(clazz)) {
                return clazz;
            }
            for (Type current : getAnyGenericInterface(clazz)) {
                if (current instanceof ParameterizedType) {
                    for (Type currentGeneric : ((ParameterizedType) current).getActualTypeArguments()) {
                        if (lookingFor.isAssignableFrom((Class<?>) currentGeneric)) {
                            return currentGeneric;
                        }
                    }
                }
            }
            throw new IOException(String.format("%s has no %s as generic parameter", classAsString, lookingFor.getSimpleName()));
        } catch (final ClassNotFoundException e) {
            throw new IOException(e);
        }
    }

    private List<Type> getAnyGenericInterface(final Class<?> clazz) {
        List<Type> result = toList(clazz.getGenericInterfaces());
        if (null != clazz.getSuperclass() && !clazz.getSuperclass().equals(Object.class)) {
            result.add(clazz.getGenericSuperclass());
        }
        return result;
    }

    private <U> List<U> toList(U[] array) {
        return Arrays.stream(array).collect(Collectors.toList());
    }

}