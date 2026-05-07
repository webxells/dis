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
package com.webxells.dis.memory;

import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import java.util.Objects;

@Description("Writes data to memory transaction")
public class MemoryOutputConfig implements OutputConfig {
    @Description("Reference to this handler")
    @Required
    private String name;
    @Description("Overwrites existent transactions")
    public boolean overwrite;
    @Description("Appends to an existent transactions")
    public boolean append;

    public MemoryOutputConfig() { }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(final boolean overwrite) {
        this.overwrite = overwrite;
    }

    public boolean isAppend() {
        return append;
    }

    public void setAppend(final boolean append) {
        this.append = append;
    }

    @Override
    public String getName() {
        return Objects.requireNonNull(name);
    }

    @Override
    public String getType() {
        return MemoryOutput.class.getName();
    }

    public void setName(final String name) {
        this.name = Objects.requireNonNull(name);
    }
}