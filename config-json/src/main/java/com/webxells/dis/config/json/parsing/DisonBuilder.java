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
package com.webxells.dis.config.json.parsing;

import com.webxells.dis.config.json.parsing.plugins.*;
import com.webxells.dis.config.json.parsing.intern.PluginFactory;
import com.webxells.dis.config.json.parsing.plugins.Error;
import com.webxells.dis.config.json.parsing.plugins.Math;
import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;

public class DisonBuilder {
    private Set<DisonErrorStrategy> errorFlag = DisonErrorStrategy.ALL;
    private Path path;
    private final PluginFactory pluginFactory = new PluginFactory();
    private boolean mapConfiguration;
    private String mappingRoot;

    public DisonBuilder addFeature(final DisonPluginManager plugin) {
        pluginFactory.register(plugin);
        return this;
    }

    public DisonBuilder mapConfiguration() {
        mapConfiguration = true;
        return this;
    }

    public DisonBuilder mapConfiguration(final String mappingRoot) {
        mapConfiguration();
        this.mappingRoot = mappingRoot;
        return this;
    }


    public DisonBuilder addAllFeatures() {
        addFeature(new Random.Manager());
        addFeature(new Math.Manager());
        addFeature(new VarParser.Manager());
        addFeature(new IncludeParser.Manager());
        addFeature(new If.Manager());
        addFeature(new TemplateParser.Manager());
        addFeature(new ForeachParser.Manager());
        addFeature(new Environment.Manager());
        addFeature(new Error.Manager());
        addFeature(new LocalFile.Manager());
        return this;
    }

    public DisonBuilder errorFlag(final Set<DisonErrorStrategy> flag) {
        errorFlag = flag;
        return this;
    }

    public DisonBuilder workingDirectory(final Path path) {
        this.path = path;
        return this;
    }

    public DisonJsonTransformer build() {
        return new DisonJsonTransformer(
                new DisonJsonTransformer.Settings(errorFlag, getPath(), pluginFactory, mapConfiguration, mappingRoot));
    }

    private String getPath() {
        return Optional.ofNullable(path)
                .map(Path::toAbsolutePath)
                .orElse(Path.of(".").toAbsolutePath().getParent())
                    .toString();
    }
}