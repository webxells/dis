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
package com.webxells.dis.localfile.output;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

@Description("Output wrapper to save to local file by dynamic generated filename per dataset")
public class DynamicLocalFileConfig implements OutputConfig {
    @Required
    @Description("reference of this output")
    private String name;

    @Required
    @Description("Child configuration used")
    private OutputConfig config;

    @Required
    @Description("path with variables of dataset")
    private String pathTemplate;

    @Description("See LocalFile::fileNameContainsDateFormat")
    private boolean fileNameContainsDateFormat;
    @Description("See LocalFile::createIfNotExists")
    private boolean createIfNotExists;
    @Description("See LocalFile::append")
    private boolean append;
    @Description("See LocalFile::fileName")
    private String fileName;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return DynamicLocalFile.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public OutputConfig getConfig() {
        return config;
    }

    public void setConfig(final OutputConfig config) {
        this.config = config;
    }

    public String getPathTemplate() {
        return pathTemplate;
    }

    public void setPathTemplate(final String pathTemplate) {
        this.pathTemplate = pathTemplate;
    }

    public boolean isFileNameContainsDateFormat() {
        return fileNameContainsDateFormat;
    }

    public void setFileNameContainsDateFormat(final boolean fileNameContainsDateFormat) {
        this.fileNameContainsDateFormat = fileNameContainsDateFormat;
    }

    public boolean isCreateIfNotExists() {
        return createIfNotExists;
    }

    public void setCreateIfNotExists(final boolean createIfNotExists) {
        this.createIfNotExists = createIfNotExists;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }
}