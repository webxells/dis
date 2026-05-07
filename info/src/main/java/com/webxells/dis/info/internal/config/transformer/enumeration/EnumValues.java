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
package com.webxells.dis.info.internal.config.transformer.enumeration;

import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.clazz.option.Enum;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.SubData;
import java.lang.reflect.Field;
import java.util.List;

public class EnumValues extends SubData<com.webxells.dis.info.internal.clazz.option.Enum, Field> {

    public EnumValues(final RootConfiguration.Configuration configuration) {
        super(configuration, List.of(
                new EnumName(configuration),
                new EnumDescription(configuration)));
    }

    @Override
    protected ForEachObject<Field> getObjects(final Enum current) {
        return current;
    }

    @Override
    public String path() {
        return "values";
    }
}