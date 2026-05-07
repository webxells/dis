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
package com.webxells.dis.base.output;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.input.linker.JoinLinker;

@Description("Loads data during output into mapping configuration")
public class LinkerOutputConfig implements OutputConfig {
    @Description("Reference of this handler")
    private String name;
    @Description("Reads input data into mapping parts")
    private JoinLinker linker;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return LinkerOutput.class.getName();
    }

    public void setName(final String name) {
        this.name = name;
    }

    public JoinLinker getLinker() {
        return linker;
    }

    public void setLinker(final JoinLinker linker) {
        this.linker = linker;
    }
}