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

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.webxells.dis.logging.simple.appender.file.DateFile;
import com.webxells.dis.logging.simple.appender.file.LastXRuns;
import com.webxells.dis.logging.simple.appender.file.NoRotation;
import com.webxells.dis.logging.simple.appender.file.RotationStrategy;
import java.lang.reflect.Type;

public record JsonRotationStrategyAdapter(JsonParser parser) implements JsonDeserializer<RotationStrategy> {

    @Override
    public RotationStrategy deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context) throws JsonParseException {
        if (json.isJsonObject()) {
            final JsonObject jsonObject = (JsonObject) json;
            if (jsonObject.has(JsonParser.TYPE_PATH_NAME)) {
                final JsonElement current = jsonObject.get(JsonParser.TYPE_PATH_NAME);
                if (current.isJsonPrimitive()) {
                    return parser.getGson().fromJson(json, getTypeByName(current.getAsString()));
                }
            }
        }
        return null;
    }

    private Class<? extends RotationStrategy> getTypeByName(final String type) {
        return switch (type) {
            case "LastXRuns" -> LastXRuns.class;
            case "NoRotation" -> NoRotation.class;
            case "DateFile" -> DateFile.class;
            default -> throw new RuntimeException("type not found: ".concat(type));
        };
    }
}