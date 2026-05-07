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
package com.webxells.dis.gis.input;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.gis.internal.ShapeManager;
import java.io.IOException;
import java.util.Map;

public class EsriShpFile implements Input<EsriShpFileConfig> {
    private final EsriShpFileConfig configuration;

    private ShapeManager shapeManager;

    public EsriShpFile(final EsriShpFileConfig configuration) {
        this.configuration = configuration;
    }

    @Override
    public int read(final MappingConfiguration from) throws InputOutputError {
        final Map<String, String> parameters = shapeManager.read();
        return from.partsBySource(configuration.getName()).stream()
                .filter(a -> null != a.getInput().getPath())
                .mapToInt(a -> {
                    final String path = a.getInput().getPath();
                    if (parameters.containsKey(path)) {
                        a.getDataset().collect(new SimpleDatasetPiece(parameters.get(path)));
                        return 1;
                    }
                    return 0;
                }).sum();
    }

    @Override
    public boolean hasNext() throws InputOutputError {
        try {
            return shapeManager.hasNext();
        } catch (final IOException e) {
            throw new InputOutputError("Error while reading shp file", e);
        }
    }

    @Override
    public String getName() {
        return configuration.getName();
    }

    @Override
    public void start() throws InputOutputError {
        try {
            shapeManager = new ShapeManager(configuration);
        } catch (final IOException e) {
            throw new InputOutputError("Error while reading shp file", e);
        }
    }

    @Override
    public void end() throws InputOutputError {
        try {
            shapeManager.end();
        } catch (final IOException e) {
            throw new InputOutputError("Error while closing shp file", e);
        }
    }
}