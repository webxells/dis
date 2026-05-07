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
package com.webxells.dis.localfile.input;

import com.webxells.dis.api.MappingPortrayal;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;

@Description("Reading MappingConfiguration to determine path to LocalFile")
public class DynamicLocalFileConfig  implements InputConfig {
    private String name;
    @Description("Child input configuration to read file")
    @Required
    private InputConfig child;
    @Description("Portrayal of filepath")
    @Required
    private MappingPortrayal source;
    @Description("If enabled, DynamicLocalFile will reload every read")
    private boolean singleRead;

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

    public MappingPortrayal getSource() {
        return source;
    }

    public void setSource(final MappingPortrayal source) {
        this.source = source;
    }

    public InputConfig getChild() {
        return child;
    }

    public void setChild(final InputConfig child) {
        this.child = child;
    }

    public boolean isSingleRead() {
        return singleRead;
    }

    public void setSingleRead(final boolean singleRead) {
        this.singleRead = singleRead;
    }
}