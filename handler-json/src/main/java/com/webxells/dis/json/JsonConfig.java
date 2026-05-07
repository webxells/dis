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
package com.webxells.dis.json;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;

public class JsonConfig {
    @Required
    @Description("Path to iterate over and match mapping path")
    private String iterationPath;
    @Description("Reference for this handler")
    @Required
    private String name;
    @Description("IterationPath leads to an object")
    @Default("false")
    private boolean singleObject;
    @Description("Makes the parser less strict when dealing malformed input")
    @Default("false")
    private boolean lenient;

    protected void validate() throws InvalidApi {
        if (null == iterationPath) {
            throw new InvalidApi("iterationPath is required");
        }
    }

    public String getIterationPath() {
        return iterationPath;
    }

    public void setIterationPath(final String iterationPath) {
        this.iterationPath = iterationPath;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public boolean isSingleObject() {
        return singleObject;
    }

    public void setSingleObject(final boolean singleObject) {
        this.singleObject = singleObject;
    }

    public boolean isLenient() {
        return lenient;
    }

    public void setLenient(final boolean lenient) {
        this.lenient = lenient;
    }
}