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
import com.webxells.dis.api.config.ParentInputConfig;
import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.api.config.description.Required;
import com.webxells.dis.api.error.InvalidApi;
import java.util.List;

@Description("Handles for child Input its MultiResource (multiple receiving of data) ")
public class MultipleResourcesInputConfig implements ParentInputConfig {
    @Required
    @Description("Reference of this handler")
    private String name;
    @Required
    @Description("Child Input configuration ")
    private InputConfig input;
    @Description("Ignores error of resource and stops receiving data")
    @Default("false")
    private boolean childErrorToStop;

    @Override
    public void validate() throws InvalidApi {
        if (null == input) {
            throw new InvalidApi("Input required");
        }
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return MultipleResourcesInput.class.getName();
    }

    public InputConfig getInput() {
        return input;
    }

    public void setInput(final InputConfig input) {
        this.input = input;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public List<InputConfig> getChildren() {
        return List.of(input);
    }

    public void setChildErrorToStop(final boolean childErrorToStop) {
        this.childErrorToStop = childErrorToStop;
    }

    public boolean isChildErrorToStop() {
        return childErrorToStop;
    }
}