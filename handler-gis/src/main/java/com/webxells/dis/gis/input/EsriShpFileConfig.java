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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.gis.PointDefinition;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.locationtech.proj4j.CoordinateReferenceSystem;

@Description("Reads an esri shapefile")
public class EsriShpFileConfig implements InputConfig {
    @Description("Path to shp file")
    @Required
    private String file;
    @Description("Reference of this handler")
    @Required
    private String name;
    private CoordinateReferenceSystem inputSpatialReferenceSystem;
    private CoordinateReferenceSystem outputSpatialReferenceSystem;
    @Description("Charset of additional data")
    @Default("UTF-8")
    private Charset charset = StandardCharsets.UTF_8;
    @Description("Date format")
    @Default("yyyy-MM-dd")
    private String dateFormat = "yyyy-MM-dd";

    @Description("Name of path for shapes coordinates")
    @Default("the_geom")
    private String shapePathName = "the_geom";

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return EsriShpFile.class.getName();
    }


    public void setName(final String name) {
        this.name = name;
    }

    public String getFile() {
        return file;
    }

    public void setFile(final String file) {
        this.file = file;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(final String charset) {
        this.charset =  Charset.forName(charset);
    }

    public CoordinateReferenceSystem getInputSpatialReferenceSystem() {
        return inputSpatialReferenceSystem;
    }

    @Description("EPSG input system")
    public void setInputSpatialReferenceSystem(final String inputSpatialReferenceSystem) {
        this.inputSpatialReferenceSystem = PointDefinition.assertValidReferenceSystem(inputSpatialReferenceSystem);
    }

    public CoordinateReferenceSystem getOutputSpatialReferenceSystem() {
        return outputSpatialReferenceSystem;
    }


    @Description("EPSG output system")
    @Required
    public void setOutputSpatialReferenceSystem(final String outputSpatialReferenceSystem) {
        this.outputSpatialReferenceSystem = PointDefinition.assertValidReferenceSystem(outputSpatialReferenceSystem);
    }

    public String getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(final String dateFormat) {
        this.dateFormat = dateFormat;
    }

    public String getShapePathName() {
        return shapePathName;
    }

    public void setShapePathName(final String shapePathName) {
        this.shapePathName = shapePathName;
    }
}