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
package com.webxells.dis.sql.refinement.error;

import com.webxells.dis.api.config.description.Default;
import com.webxells.dis.api.config.description.Description;

@Description("An static value. Can be used to set to null")
public class Static implements Strategy {
    @Default("null")
    private String value;

    @Override
    public String getValueForErrorEnforcement() {
        return value;
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
