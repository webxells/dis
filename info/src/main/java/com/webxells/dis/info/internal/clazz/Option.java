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
package com.webxells.dis.info.internal.clazz;

import com.webxells.dis.info.internal.Annotation;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.clazz.option.Enum;
import com.webxells.dis.info.internal.clazz.option.ImplementationOf;
import com.webxells.dis.info.internal.clazz.option.OptionType;
import com.webxells.dis.info.internal.clazz.option.Primitive;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Optional;

public class Option {
    private final Class<?> rawOptionType;
    private final Method method;
    private final Field field;
    private final OptionType type;

    public Option(final Method method, final Class<?> parent) {
        if (1 != method.getParameterCount()) {
            throw new IllegalArgumentException("Only Methods with one parameter are accepted");
        }
        this.method = method;
        rawOptionType = method.getParameterTypes()[0];
        field = getField(method.getName(), parent);
        type = createType(method.getGenericParameterTypes()[0]);
    }

    public Option(final Field field) {
        this.method = null;
        rawOptionType = field.getType();
        this.field = field;
        type = createType(field.getGenericType());
    }

    @SuppressWarnings("unchecked")
    private OptionType createType(final Type firstType) {
        if (rawOptionType.isEnum()) {
            return new Enum((Class<? extends java.lang.Enum<?>>) rawOptionType);
        }
        final ImplementationOf result = new ImplementationOf(firstType);
        if (Primitive.isAssignable(result.getImplementationClass())) {
            return new Primitive(result.getImplementationClass());
        }
        return result;
    }

    public String getName() {
        return Optional.ofNullable(method)
                .map(a -> toFieldName(a.getName()))
                .orElseGet(() -> field.getName());
    }

    private Field getField(final String name, final Class<?> clazz) {
        final String fieldName = toFieldName(name);
        return Implementation.getFields(clazz).stream()
                .filter(a -> a.getName().equals(fieldName))
                .findAny()
                .orElse(null);
    }

    private String toFieldName(final String name) {
        return name.substring(3, 4).toLowerCase() + name.substring(4);
    }

    public <T extends java.lang.annotation.Annotation> Optional<T> getAnnotation(final Class<T> clazz) {
        return Annotation.get(clazz, method, field);
    }

    public OptionType getType() {
        return type;
    }

    public Class<?> getParameter() {
        return rawOptionType;
    }
}