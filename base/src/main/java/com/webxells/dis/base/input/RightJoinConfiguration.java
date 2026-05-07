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

@Description("Similar to base.input.Join but instead of reading only once from child, this will be read until no data" +
        " of children ist returned")
public class RightJoinConfiguration implements InputConfig {
    @Required
    @Alias("child")
    private InputConfig childConfig;
    @Required
    @Alias("root")
    private InputConfig rootConfig;
    @Required
    @Description("Reference of this handler")
    private String name;
    @Description("Continue reading from child despite no added data sets")
    private boolean continueDespiteNoData = false;

    public InputConfig getChildConfig() {
        return childConfig;
    }

    public void setChildConfig(final InputConfig childConfig) {
        this.childConfig = childConfig;
    }

    public InputConfig getRootConfig() {
        return rootConfig;
    }

    public void setRootConfig(final InputConfig rootConfig) {
        this.rootConfig = rootConfig;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    @Override
    public String getType() {
        return RightJoin.class.getName();
    }

    public boolean isContinueDespiteNoData() {
        return continueDespiteNoData;
    }

    public void setContinueDespiteNoData(final boolean continueDespiteNoData) {
        this.continueDespiteNoData = continueDespiteNoData;
    }
}