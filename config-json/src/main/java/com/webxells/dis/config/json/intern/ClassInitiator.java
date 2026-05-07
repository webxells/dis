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
package com.webxells.dis.config.json.intern;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.lang.reflect.Constructor;
import com.webxells.dis.boot.KnownTypeMapping;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.stream.Collectors;

public class ClassInitiator {
    public static <T> T createInstanceByType(final Class<T> interfaze, final JsonElement json, final Object ...constructorArgs) throws IOException {
        final String resultingClassName = getResultingClassForInterface(interfaze, json);
        final Class<?> resultingClass;
        try {
            resultingClass = loadClass(resultingClassName);
        } catch (final ClassNotFoundException e) {
            throw new IOException("Could not find ".concat(resultingClassName));
        }
        if (!interfaze.isAssignableFrom(resultingClass)) {
            throw new IOException(String.format("Could not assign resulting class %s to interface %s",
                    resultingClassName, interfaze.getName()));
        }
        try {
            return getConstructor((Class<T>) resultingClass, constructorArgs).newInstance(constructorArgs);
        } catch (final InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new IOException("Could not load instance for interface ".concat(interfaze.getName()), e);
        }
    }

    public static String getResultingClassForInterface(final Class<?> interfaze, final JsonElement json) throws IOException {
        return getTypeByJsonObject(json, KnownTypeMapping.getKnown(interfaze));
    }

    public static  Class<?> loadClass(final String classAsString) throws ClassNotFoundException {
        try {
            return Class.forName(classAsString, true, ClassLoader.getSystemClassLoader());
        } catch (final ClassNotFoundException | NoClassDefFoundError ignored) { }
        return Class.forName(classAsString, true, ClassLoader.getPlatformClassLoader());
    }

    public static String getTypeByJsonObject(final JsonElement element, final String fallbackClass) throws IOException {
        final String type = getJsonType(element);
        if (null != type) {
            return type;
        }
        if (null == fallbackClass) {
            throw new IOException("Invalid json config structure - type should be declared");
        }
        return fallbackClass;
    }

    private static <T> Constructor<T> getConstructor(final Class<T> resultingClass, final Object[] constructorArgs) throws IOException {
        return Arrays.stream(resultingClass.getConstructors())
                .filter(a -> a.getParameterCount() == constructorArgs.length)
                .filter(a -> assignableParameters(a.getParameterTypes(), constructorArgs))
                .findAny()
                .map(a -> (Constructor<T>) a)
                .orElseThrow(() -> new IOException(String.format("Could not find right constructor%s for class %s",
                        Arrays.stream(constructorArgs)
                                .map(Object::toString)
                                .collect(Collectors.toList()),
                        resultingClass.getName())));
    }

    private static boolean assignableParameters(final Class<?>[] parameterTypes, final Object[] constructorArgs) {
        for (int i = parameterTypes.length - 1; 0 < i--;) {
            if (!parameterTypes[i].isAssignableFrom(constructorArgs[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    private static String getJsonType(final JsonElement element) {
        if (element.isJsonObject()) {
            final JsonObject object = element.getAsJsonObject();
            final JsonElement type = object.get("type");
            if (!(null == type || type.isJsonNull() || !type.isJsonPrimitive())) {
                return type.getAsString();
            }
        }
        return null;
    }
}