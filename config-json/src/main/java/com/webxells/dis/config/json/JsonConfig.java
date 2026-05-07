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
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.webxells.dis.api.config.DisConfig;
import com.webxells.dis.api.config.JobConfig;
import com.webxells.dis.api.workflow.DisSystem;
import com.webxells.dis.api.workflow.SystemSupplier;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;

public class JsonConfig implements DisConfig {
    protected static final Gson GSON = new GsonBuilder()
            .registerTypeAdapterFactory(new GsonDisAdapterFactory())
            .create();

    protected List<JobConfig> configurations;
    protected SystemSupplier<?> system;

    public JsonConfig(final File file) throws IOException {
        this(readConfigFile(file));
    }

    private static String readConfigFile(final File file) throws IOException {
        if (!(file.isFile() && file.canRead())) {
            throw new IOException("invalid config file");
        }
        return new String(Files.readAllBytes(file.toPath()));
    }

    public JsonConfig(final String json) {
        final JsonObject root = asJsonObject(JsonParser.parseString(json));
        configurations = parseDisConfig(root);
        system = parseWorkflow(root);
    }

    public JsonConfig(final List<JobConfig> configuration, final SystemSupplier<?> system) {
        this.configurations = configuration;
        this.system = system;
    }

    @Override
    public List<JobConfig> getConfigurations() {
        return configurations;
    }

    @Override
    public void setConfigurations(final List<JobConfig> configurations) {
        this.configurations = configurations;
    }

    @Override
    public <T extends DisSystem> SystemSupplier<T> getSystemSupplier() {
        return (SystemSupplier<T>) system;
    }

    @Override
    public <T extends DisSystem> void setSystemSupplier(final SystemSupplier<T> systemSupplier) {
        system = systemSupplier;
    }

    protected List<JobConfig> parseDisConfig(final JsonObject root) {
        final List<JobConfig> result = new LinkedList<>();
        root.getAsJsonArray("configurations")
                .forEach(current -> {
                    if (current.isJsonObject()) {
                        result.add(GSON.fromJson(current, JobConfig.class));
                    }
                });
        return result;
    }

    private SystemSupplier<?> parseWorkflow(final JsonObject root) {
        return GSON.fromJson(asJsonObject(root.get("system")), SystemSupplier.class);
    }

    protected JsonObject asJsonObject(final JsonElement element) {
        assertValidJsonObject(element);
        return element.getAsJsonObject();
    }

    protected void assertValidJsonObject(final JsonElement tree) {
        if (null == tree || tree.isJsonNull() || !tree.isJsonObject()) {
            throw new RuntimeException("Invalid json object");
        }
    }
}