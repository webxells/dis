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
package com.webxells.dis.csv;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.resource.Resource;
import java.util.List;

@Description("Writes csv data")
public class CsvOutputConfig extends CsvConfig implements OutputConfig {
    @Description("Destination to send the csv data to")
    @Required
    private Resource sender;
    @Description("Sets exclusive header fields - otherwise mapping path of all parts are considered as headers")
    private List<String> headerFields;

    public CsvOutputConfig() {}

    public CsvOutputConfig(final String name, final Resource sender) {
        this.name = name;
        this.sender = sender;
    }

    @Override
    public String getType() {
        return CsvOutput.class.getName();
    }

    public Resource getSender() {
        return sender;
    }

    public void  setSender(final Resource resource) {
        sender = resource;
    }

    public List<String> getHeaderFields() {
        return headerFields;
    }

    public void setHeaderFields(final List<String> headerFields) {
        this.headerFields = headerFields;
    }

}