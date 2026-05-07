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
package com.webxells.dis.info.internal.config.transformer.option;

import com.webxells.dis.info.internal.clazz.Option;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.SinglePartKnownMultiValue;

public class RequiredCondition extends SinglePartKnownMultiValue<Option> {
    private final String name;

    public RequiredCondition(final RootConfiguration.Configuration configuration,
                             final String name, final String[] values) {
        super(values, configuration);
        this.name = name;
    }

    @Override
    public String path() {
        return name;
    }

}