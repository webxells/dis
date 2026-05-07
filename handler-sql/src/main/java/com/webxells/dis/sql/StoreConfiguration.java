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
package com.webxells.dis.sql;

import com.webxells.dis.api.config.OutputConfig;

public class StoreConfiguration extends SqlConfiguration implements OutputConfig {
    private String noValueReplacement;
    private boolean skipUnusedParameters;

    @Override
    public String getType() {
        return Store.class.getName();
    }

    public String getNoValueReplacement() {
        return noValueReplacement;
    }

    public void setNoValueReplacement(final String noValueReplacement) {
        this.noValueReplacement = noValueReplacement;
    }

    public void setSkipUnusedParameters(final boolean skipUnusedParameters) {
        this.skipUnusedParameters = skipUnusedParameters;
    }

    public boolean isSkipUnusedParameters() {
        return skipUnusedParameters;
    }
}