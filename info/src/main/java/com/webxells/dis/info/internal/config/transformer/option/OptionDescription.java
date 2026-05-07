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

import com.webxells.dis.api.config.description.Description;
import com.webxells.dis.info.internal.clazz.Option;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.SinglePart;

public class OptionDescription extends SinglePart<Option> {

    public OptionDescription(final RootConfiguration.Configuration configuration) {
        super(configuration);
    }

    @Override
    protected String getValue(final Option current) {
        return current.getAnnotation(Description.class)
                .map(Description::value)
                .orElse(null);
    }

    @Override
    public String path() {
        return "description";
    }
}