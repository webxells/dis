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
package com.webxells.dis.info.internal.clazz.option;

import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.config.RootConfiguration;
import com.webxells.dis.info.internal.config.transformer.enumeration.EnumValues;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.function.Consumer;

public class Enum implements OptionType, ForEachObject<Field> {
    private final Class<? extends java.lang.Enum<?>> enumClass;

    public Enum(final Class<? extends java.lang.Enum<?>> enumClass) {
        this.enumClass = enumClass;
    }

    @Override
    public String getType() {
        return "enum";
    }

    @Override
    public int renderType(final RootConfiguration.Configuration configuration,
                          final MappingConfiguration mappingConfiguration) {
        return new EnumValues(configuration)
                .transform(this, mappingConfiguration);
    }

    @Override
    public void forEachImplementation(final Consumer<Field> consumer) {
        Arrays.stream(enumClass.getFields())
                .forEach(consumer);
    }
}