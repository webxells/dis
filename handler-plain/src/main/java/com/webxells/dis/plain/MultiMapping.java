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
package com.webxells.dis.plain;

import com.webxells.dis.api.config.DisConfigApi;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

@Description("Description how mapping of subData is treated")
public class MultiMapping implements DisConfigApi {
    public enum SourceType {
        @Description("Parsing subData as new template") MULTI_CONFIGURATION,
        @Description("Parsing subData as value list") MULTI_VALUE;
    }

    @Description("Template for subData - required by sourceType MULTI_CONFIGURATION")
    private String template;
    @Description("Value to put between values - required by sourceType MULTI_VALUE")
    private String delimiter = "";
    @Description("Value to put before the generated string")
    private String before = "";
    @Description("Value to put after the generated string")
    private String after = "";
    @Description("Type of this multi Mapping")
    @Default("MULTI_CONFIGURATION")
    private SourceType sourceType = SourceType.MULTI_CONFIGURATION;

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    public String getBefore() {
        return before;
    }

    public void setBefore(final String before) {
        this.before = before;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(final String after) {
        this.after = after;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public void setSourceType(final SourceType sourceType) {
        this.sourceType = sourceType;
    }
}