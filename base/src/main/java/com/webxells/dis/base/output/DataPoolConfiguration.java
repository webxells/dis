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
import com.webxells.dis.api.resource.Resource;

public class DataPoolConfiguration implements OutputConfig {
    private String name;
    private Resource sender;
    private DataPoolOutput.NoDataStrategy noDataStrategy = DataPoolOutput.NoDataStrategy.IGNORE;

    @Override
    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public Resource getSender() {
        return sender;
    }

    public void setSender(final Resource sender) {
        this.sender = sender;
    }

    public DataPoolOutput.NoDataStrategy getNoDataStrategy() {
        return noDataStrategy;
    }

    public void setNoDataStrategy(final DataPoolOutput.NoDataStrategy noDataStrategy) {
        this.noDataStrategy = noDataStrategy;
    }

    @Override
    public String getType() {
        return DataPoolOutput.class.getName();
    }
}