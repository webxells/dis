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
package com.webxells.dis.gis.internal;

import com.webxells.dis.api.Logger;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.gis.input.EsriShpFileConfig;
import com.webxells.dis.gis.internal.shape.Shape;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import org.locationtech.proj4j.CRSFactory;
import org.locationtech.proj4j.CoordinateReferenceSystem;

public class ShapeManager {
    private static final Logger LOGGER = LoggerProxyFactory.logger(ShapeManager.class);
    private static final CRSFactory CRS_FACTORY = new CRSFactory();

    private final ShpReader shpReader;
    private final DBaseFileReader dBaseFileReader;
    private final CoordinateReferenceSystem inputCrs;
    private final CoordinateReferenceSystem outputSrs;
    private final String shapePathName;


    public ShapeManager(final EsriShpFileConfig config) throws IOException {
        final String file = config.getFile();
        shapePathName = config.getShapePathName();
        shpReader = new ShpReader(new File(file));
        dBaseFileReader = new DBaseFileReader(getPathOfCorrectFile(file, "dbf"), config.getDateFormat(),
                config.getCharset());
        this.inputCrs = getInputCrs(config.getInputSpatialReferenceSystem(), file);
        this.outputSrs = config.getOutputSpatialReferenceSystem();
    }

    public Map<String, String> read() throws InputOutputError {
        try {
            Optional<Map<String, String>> attributes;
            ShpFileRecord record;
            do {
                attributes = dBaseFileReader.read();
                record = shpReader.read(inputCrs);
            } while (attributes.isEmpty());
            final Map<String, String> result = attributes.get();
            if (null != record.getShape()) {
                result.put(shapePathName, transform(record.getShape()));
            }
            return result;
        } catch (final IOException e) {
            throw new InputOutputError("Error reading shape", e);
        }
    }

    public void end() throws IOException {
        shpReader.close();
        dBaseFileReader.close();
    }

    public boolean hasNext() throws IOException {
        return shpReader.hasNext();
    }

    private CoordinateReferenceSystem getInputCrs(final CoordinateReferenceSystem inputSpatialReferenceSystem,
                                                  final String file) throws IOException {
        if (inputSpatialReferenceSystem == null) {
            LOGGER.debug("No defined inputSpatialReferenceSystem - parsing .prj file...");
            return parseProjFile(getPathOfCorrectFile(file, "prj"));
        }
        return inputSpatialReferenceSystem;
    }

    private CoordinateReferenceSystem parseProjFile(final Path filePath) throws IOException {
        if (Files.exists(filePath)) {
            return PrjFileParser.parseToLocationtech(Files.readString(filePath), CRS_FACTORY);
        }
        throw new IOException(String.format("No prj file exists (%s) - please provide an inputSpatialReferenceSystem", filePath.toAbsolutePath()));
    }

    private Path getPathOfCorrectFile(final String file, final String correctSuffix) {
        if (file.endsWith(correctSuffix)) {
            return Path.of(file);
        }
        return Path.of(file.substring(0, file.lastIndexOf(".") + 1).concat(correctSuffix));
    }

    private String transform(final Shape shape) {
        if (null == outputSrs || inputCrs.equals(outputSrs)) {
            return shape.toWktString();
        }
        return shape.transform(outputSrs).toWktString();
    }
}