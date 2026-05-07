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
package com.webxells.dis.base.input;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;

@Description("Reads data from internal data pool")
public class DataPoolConfiguration implements InputConfig {
    @Required
    @Description("Reference of this handler / data index")
    private String name;
    @Description("Mime type of the file")
    private String fileType;
    @Description("Name of the file")
    private String fileName;
    @Description("Loads files from a source and puts it into the data pool")
    private Resource receiver;
    @Description("Mapping part that contains the file name")
    private MappingPortrayal fileNamePortrayal;
    @Description("Mapping part that contains the mime type of the file")
    private MappingPortrayal fileTypePortrayal;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return DataPoolInput.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(final String fileType) {
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public MappingPortrayal getFileNamePortrayal() {
        return fileNamePortrayal;
    }

    public void setFileNamePortrayal(final MappingPortrayal fileNamePortrayal) {
        this.fileNamePortrayal = fileNamePortrayal;
    }

    public MappingPortrayal getFileTypePortrayal() {
        return fileTypePortrayal;
    }

    public void setFileTypePortrayal(final MappingPortrayal fileTypePortrayal) {
        this.fileTypePortrayal = fileTypePortrayal;
    }
}