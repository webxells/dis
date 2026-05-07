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
package com.webxells.dis.logging.simple.internal;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.webxells.dis.logging.simple.appender.Appender;
import com.webxells.dis.logging.simple.appender.file.RotationStrategy;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonParser {
    private static class ExcludeNonPublic implements ExclusionStrategy {
        @Override
        public boolean shouldSkipField(final FieldAttributes f) {
            return !f.hasModifier(Modifier.PUBLIC);
        }

        @Override
        public boolean shouldSkipClass(final Class<?> clazz) {
            return false;
        }
    }

    public static final String TYPE_PATH_NAME = "type";

    private final Gson gson = new GsonBuilder()
            .setExclusionStrategies(new ExcludeNonPublic())
            .registerTypeAdapter(RotationStrategy.class, new JsonRotationStrategyAdapter(this))
            .create();
    private final JsonObject root;

    public JsonParser(final String filePath) throws FileNotFoundException {
        root = com.google.gson.JsonParser.parseReader(new FileReader(filePath)).getAsJsonObject();
    }

    public JsonParser(final InputStream file) throws IOException {
        root = com.google.gson.JsonParser.parseString(new String(file.readAllBytes())).getAsJsonObject();
    }

    public <T extends Enum<T>> Optional<T> getAsEnum(final String path, final Class<T> enumClass) {
        return getAsEnum(root, path, enumClass);
    }

    public <T extends Enum<T>> List<T> getAsEnumList(final String path, final Class<T> enumClass) {
        return getAsEnumList(root, path, enumClass);
    }

    private <T extends Enum<T>> List<T> getAsEnumList(final JsonObject object, final String path, final  Class<T> enumClass) {
        return getAsArray(object, path)
                .map(a -> getAsEnum(a, enumClass))
                .toList();
    }

    private Stream<JsonElement> getAsArray(final JsonObject object, final String path) {
        if (object.has(path)) {
            final JsonElement array = object.get(path);
            if (array.isJsonArray()) {
                return array.getAsJsonArray()
                        .asList()
                        .stream();
            }
        }
        return Stream.of();
    }

    private <T extends Enum<T>> Optional<T> getAsEnum(final JsonObject object, final String path, final  Class<T> enumClass) {
        return getAsString(object, path)
                .map(a -> Enum.valueOf(enumClass, a));
    }

    private <T extends Enum<T>> T getAsEnum(final JsonElement element, final  Class<T> enumClass) {
        return Optional.ofNullable(element.isJsonPrimitive() ? element.getAsString() : null)
                .map(a -> Enum.valueOf(enumClass, a))
                .orElse(null);
    }

    public <T extends Enum<T>> Map<String, T> getAsEnumMap(final String path, final Class<T> enumClass) {
        return getAsEnumMap(root, path, enumClass);
    }

    public <T extends Enum<T>> Map<String, T> getAsEnumMap(final JsonObject object, final String path, final Class<T> enumClass) {
        return getAsMap(object, path).entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, a -> getAsEnum(a.getValue(), enumClass), (a, b) -> b));
    }

    private Map<String, JsonElement> getAsMap(final JsonObject object, final String path) {
        return Optional.ofNullable(object.get(path))
                .filter(JsonElement::isJsonObject)
                .map(JsonElement::getAsJsonObject)
                .map(JsonObject::asMap)
                .orElseGet(Map::of);
    }

    public Optional<String> getAsString(final JsonObject object, final String path) {
        return Optional.ofNullable(object.has(path) ? object.getAsJsonPrimitive(path).getAsString() : null);
    }

    public boolean getAsBoolean(final String path, final boolean defaultValue) {
        return Optional.ofNullable(root.has(path) ? root.getAsJsonPrimitive(path).getAsBoolean() : null)
                .orElse(defaultValue);
    }

    public Optional<String> getAsString(final String path) {
        return getAsString(root, path);
    }

    public List<Appender> getAppenders(final String path) {
        final List<Appender> result = new LinkedList<>();
        root.getAsJsonArray(path).forEach(current -> {
            result.add(objectToAppenderByType(current.getAsJsonObject()));
        });
        return result;
    }

    private Appender objectToAppenderByType(final JsonObject object) {
        if (object.has(TYPE_PATH_NAME)) {
            return getAsEnum(object, TYPE_PATH_NAME, Appender.REGISTERED_TYPES.class)
                    .map(Appender.REGISTERED_TYPES::clazz)
                    .map(a -> gson.fromJson(object, a))
                    .orElseThrow(() -> new RuntimeException("Could not create appender"));
        }
        throw new RuntimeException("Could not find type path: ".concat(TYPE_PATH_NAME));
    }

    Gson getGson() {
        return gson;
    }
}