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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.description.Alias;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.input.linker.JoinLinker;
import java.util.List;
import java.util.Objects;

@Description("Joins multiple Input configurations by specific JoinLinker")
public class JoinConfiguration implements InputConfig {
    @Required
    @Description("Reference of this handler")
    private String name;
    @Required
    @Description("Configuration that child JoinLinkers depends")
    @Alias("root")
    private InputConfig rootConfig;
    @Required
    @Description("To link root with specific child")
    private List<JoinLinker> children;

    public void setChildren(final List<JoinLinker> children) {
        this.children = Objects.requireNonNull(children);
    }

    public void setName(final String name) {
        this.name = Objects.requireNonNull(name);
    }

    public List<JoinLinker> getChildren() {
        return Objects.requireNonNull(children);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return Join.class.getName();
    }

    public InputConfig getRootConfig() {
        return rootConfig;
    }

    public void setRootConfig(final InputConfig rootConfig) {
        this.rootConfig = rootConfig;
    }
}