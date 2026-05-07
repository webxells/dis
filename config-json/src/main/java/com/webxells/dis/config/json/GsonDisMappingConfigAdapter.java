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
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.base.config.SimpleMappingConfiguration;
import com.webxells.dis.config.json.intern.ClassInitiator;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

class GsonDisMappingConfigAdapter<T> extends ValidatingReader<T> {
    private final Gson gson;
    private final GsonDisAdapterFactory factory;

    private boolean insideSubData;

    public GsonDisMappingConfigAdapter(final Gson gson, final GsonDisAdapterFactory factory) {
        this.gson = gson;
        this.factory = factory;
    }

    @Override
    T readClass(final JsonReader in) throws IOException {
        final JsonElement plain = gson.fromJson(in, JsonElement.class);
        return (T) readByObject(plain);
    }

    private MappingConfiguration readByObject(final JsonElement plain) throws IOException {
        final SimpleMappingConfiguration result = new SimpleMappingConfiguration();
        assertValidMappingConfigJson(plain);
        for (JsonElement jsonMappingConfig : plain.getAsJsonObject().getAsJsonArray("parts")) {
            if (jsonMappingConfig.isJsonNull()) {
                continue;
            }
            final MappingPart config = ClassInitiator.createInstanceByType(MappingPart.class, jsonMappingConfig, result);
            try {
                GsonUseFieldForSetterAdapter.fillExisting(config, jsonMappingConfig.getAsJsonObject(), gson);
            } catch (final InvocationTargetException | IllegalAccessException e) {
                throw new IOException("Could not fill instance", e);
            }
            validate(config);
            result.addPart(config);
        }
        return result;
    }

    private void validate(final DisApi config) throws IOException {
        try {
            config.validate();
        } catch (final InvalidApi e) {
            throw new IOException("Invalid config", e);
        }
    }

    private void assertValidMappingConfigJson(final JsonElement plain) throws IOException {
        assertValidJsonObjectWithKey(plain, "parts");
        if (!plain.getAsJsonObject().get("parts").isJsonArray()) {
            throw new IOException("Invalid mapping configuration");
        }
    }

    private void assertValidJsonObjectWithKey(final JsonElement plain, final String index) throws IOException {
        if (!(plain.isJsonObject() && plain.getAsJsonObject().has(index))) {
            throw new IOException("No json object with key: ".concat(index));
        }
    }

}