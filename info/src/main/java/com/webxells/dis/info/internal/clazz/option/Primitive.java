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
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Primitive implements OptionType {
    private static final Map<Class<?>, String> CLASS_MAP = new HashMap<>() {{
                put(Integer.class, "int");
                put(int.class, "int");
                put(Byte.class, "byte");
                put(byte.class, "byte");
                put(Character.class, "char");
                put(char.class, "char");
                put(Boolean.class, "boolean");
                put(boolean.class, "boolean");
                put(Double.class, "double");
                put(double.class, "double");
                put(Float.class, "float");
                put(float.class, "float");
                put(Long.class, "long");
                put(long.class, "long");
                put(Short.class, "short");
                put(short.class, "short");
                put(String.class, "string");
    }};

    private final String type;

    public Primitive(final Class<?> clazz) {
        type = Optional.ofNullable(CLASS_MAP.get(clazz))
                .orElseThrow(() -> new IllegalArgumentException("Unknown Primitive type: " + clazz.getName()));
    }

    public static boolean isAssignable(final Class<?> firstParameter) {
        return CLASS_MAP.containsKey(firstParameter);
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public int renderType(final RootConfiguration.Configuration configuration, final MappingConfiguration mappingConfiguration) {
        return 0;
    }
}