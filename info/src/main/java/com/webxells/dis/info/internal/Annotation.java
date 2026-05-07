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
package com.webxells.dis.info.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

public class Annotation {
    public static <T extends java.lang.annotation.Annotation> Optional<T> get(final Class<T> clazz, final Method method, final Field field) {
        return Optional.ofNullable(extract(clazz, method)
                .orElse(extract(clazz, field)
                        .orElse(null)));
    }

    public static <T extends java.lang.annotation.Annotation> Optional<T> extract(final Class<T> clazz, final Method method) {
        if (null == method) {
            return Optional.empty();
        }
        return extractAnnotation(clazz, method.getAnnotations());
    }

    public static <T extends java.lang.annotation.Annotation> Optional<T> extract(final Class<T> clazz, final Field field) {
        if (null == field) {
            return Optional.empty();
        }
        return extractAnnotation(clazz, field.getAnnotations());
    }

    private static <T extends java.lang.annotation.Annotation> Optional<T> extractAnnotation(
            final Class<T> clazz,
            final java.lang.annotation.Annotation[] annotations) {
        return Arrays.stream(annotations)
                .filter(a -> a.annotationType() == clazz)
                .map(clazz::cast)
                .findAny();
    }
}