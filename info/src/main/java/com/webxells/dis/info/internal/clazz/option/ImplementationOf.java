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

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.MappingConfiguration;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.config.TriggerConfig;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.info.internal.config.RootConfiguration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

public class ImplementationOf implements OptionType {
    private static final Map<Class<?>, Class<?>> CONFIG_TO_IMPLEMENTATION = Map.of(
            InputConfig.class, Input.class,
            OutputConfig.class, Output.class,
            TriggerConfig.class, Trigger.class
    );

    private final Class<?> typeOf;

    public ImplementationOf(final Type firstType) {
        typeOf = getClass(firstType);
    }

    /*
     * @todo: resolve Map of List or List of List of List...
     */
    private Class<?> getClass(final Type firstType) {
        if (firstType instanceof ParameterizedType asParameterizedType) {
            final Type rawType = asParameterizedType.getRawType();
            if (rawType instanceof Class<?> rawClass) {
                if (Collection.class.isAssignableFrom(rawClass)) {
                    return getClass(asParameterizedType.getActualTypeArguments()[0]);
                }
                if (Map.class.isAssignableFrom(rawClass)) {
                    return getClass(asParameterizedType.getActualTypeArguments()[1]);
                }
            }
        } else if (firstType instanceof Class<?> asClass) {
            return CONFIG_TO_IMPLEMENTATION.getOrDefault(asClass, asClass);
        }
        throw new IllegalArgumentException("Could not evaluate type: " + firstType.getTypeName());
    }

    @Override
    public String getType() {
        return "implementation";
    }

    @Override
    public int renderType(final RootConfiguration.Configuration configuration, final MappingConfiguration mappingConfiguration) {
        return new ImplementationOfType(configuration).transform(this, mappingConfiguration);
    }

    public String getClassName() {
        return typeOf.getName();
    }

    public Class<?> getImplementationClass() {
        return typeOf;
    }
}