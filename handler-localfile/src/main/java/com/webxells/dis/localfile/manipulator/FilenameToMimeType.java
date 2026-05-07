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
package com.webxells.dis.localfile.manipulator;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.MappingPart;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.error.InvalidDatasetException;

import com.webxells.dis.api.manipulator.SingleCallForAllValuesManipulator;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.localfile.intern.WiseFileNameMap;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Description("Looks up mimetype by file extension")
public class FilenameToMimeType implements SingleCallForAllValuesManipulator {
    static {{
        WiseFileNameMap.initialize();
    }}

    @Description("Retrieves this type, if no other was found")
    @Default("application/octet-stream")
    private String defaultType = "application/octet-stream";
    @Description("Looks here for mimetype, else takes current mapping part")
    private MappingPortrayal portrayal;
    @Description("Defines mapping between file extensions and custom types")
    private Map<String, String> customTypes;

    @Override
    public void manipulate(final DatasetPiece currentPiece, final MappingPart mappingPart) {
        final List<DatasetPiece> part = Optional.ofNullable(portrayal)
                .flatMap(a -> mappingPart.getConfiguration().getByPortrayal(a))
                .map(a -> a.getDataset().getContent())
                .orElseGet(() -> List.copyOf(mappingPart.getDataset().getContent()));

        mappingPart.getDataset().clear();

        part.stream()
                .map(this::toMimeType)
                .forEach(a -> mappingPart.getDataset().collect(new SimpleDatasetPiece(a)));
    }

    public void setDefaultType(final String defaultType) {
        this.defaultType = defaultType;
    }

    public void setPortrayal(final MappingPortrayal portrayal) {
        this.portrayal = portrayal;
    }

    public void setCustomTypes(final Map<String, String> customTypes) {
        this.customTypes = customTypes.entrySet().stream()
                .collect(Collectors.toMap(a -> a.getKey().toLowerCase(), Map.Entry::getValue));
    }

    private String toMimeType(final DatasetPiece piece) {
        return piece.value()
                .map(String::toLowerCase)
                .map(a -> Optional.ofNullable(customTypes)
                        .map(b -> b.get(cropType(a)))
                        .orElseGet(() -> URLConnection.guessContentTypeFromName(a)))
                .orElse(defaultType);
    }

    private String cropType(final String name) {
        final int pos = name.lastIndexOf('.');
        if (pos > -1 && pos + 1 < name.length()) {
            return name.substring(pos + 1);
        }
        return null;
    }
}