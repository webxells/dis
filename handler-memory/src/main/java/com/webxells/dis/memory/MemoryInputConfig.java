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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import java.util.Objects;

@Description("Reads data from memory transaction")
public class MemoryInputConfig implements InputConfig {
    @Description("Reference to this handler")
    @Required
    private String name;
    @Description("Rolls transaction back on start")
    public boolean persistent;
    @Description("Deletes after reading")
    public boolean popInsteadOfRead;

    public MemoryInputConfig() { }

    public MemoryInputConfig(final String name) {
        this.name = name;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(final boolean persistent) {
        this.persistent = persistent;
    }

    @Override
    public String getName() {
        return Objects.requireNonNull(name);
    }

    @Override
    public String getType() {
        return MemoryInput.class.getName();
    }

    public void setName(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    public boolean isPopInsteadOfRead() {
        return popInsteadOfRead;
    }

    public void setPopInsteadOfRead(final boolean popInsteadOfRead) {
        this.popInsteadOfRead = popInsteadOfRead;
    }
}