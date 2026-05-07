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
package com.webxells.dis.boot;

import com.webxells.dis.api.config.ConfigurableByType;
import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.config.OutputConfig;
import com.webxells.dis.api.error.InvalidApi;
import com.webxells.dis.api.resource.Resource;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import static java.util.Optional.ofNullable;

public class ServiceManager {
    private static final ClassLoader classLoader = ClassLoader.getSystemClassLoader();

    public static <T> T loadByConfig(final ConfigurableByType config) {
        try {
            final Class<?> clazz = classLoader.loadClass(Objects.requireNonNull(config.getType()));
            final Constructor<?> constructor = Arrays.stream(clazz.getConstructors())
                    .filter(a -> a.getParameterCount() == 1 && ConfigurableByType.class.isAssignableFrom(a.getParameterTypes()[0]))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("No constructor found for: ".concat(clazz.getName())));
            //noinspection unchecked
            final T result = (T) constructor.newInstance(config);
            if (result instanceof DisApi disApi) {
                try {
                    disApi.validate();
                } catch (final InvalidApi e) {
                    throw new RuntimeException("Could not load api", e);
                }
            }
            return result;
        } catch (final ReflectiveOperationException e) {
            throw new RuntimeException("Failed to load typed Dis class: ".concat(config.getType()), e);
        }
    }

    public static Resource extractReceiverOfConfig(final DisApi config) {
        final Method receiverMethod = lookForReceiverGetterMethod(config);
        return extractResourceOfMethod(receiverMethod, config);
    }

    public static Resource extractSenderOfConfig(final DisApi config) {
        final Method senderMethod = lookForSenderGetterMethod(config);
        return extractResourceOfMethod(senderMethod, config);
    }

    public static Resource extractResourceOfConfig(final InputConfig config) {
        return extractReceiverOfConfig(config);
    }

    public static Resource extractResourceOfConfig(final OutputConfig config) {
        return extractSenderOfConfig(config);
    }

    public static void overwriteResourceOfConfig(final InputConfig config, final Resource resource) {
        final Method receiverMethod = lookForReceiverSetterMethod(config);
        overwriteResourceWithMethod(receiverMethod, config, resource);
    }

    public static void overwriteResourceOfConfig(final OutputConfig config, final Resource resource) {
        final Method receiverMethod = lookForSenderSetterMethod(config);
        overwriteResourceWithMethod(receiverMethod, config, resource);
    }

    private static Method lookForSenderSetterMethod(final DisApi config) {
        return lookForSpecificSetterMethod(config, "setSender");
    }

    private static Method lookForReceiverSetterMethod(final DisApi config) {
        return lookForSpecificSetterMethod(config, "setReceiver");
    }

    private static void overwriteResourceWithMethod(final Method method, final ConfigurableByType  config, final Resource resource) {
        if (null != method) {
            try {
                 method.invoke(config, resource);
            } catch (final IllegalAccessException | InvocationTargetException ignore) { }
        }
    }

    private static Method lookForSenderGetterMethod(final DisApi config) {
        return ofNullable(lookForSpecificGetterMethod(config, "getSender"))
                .orElse(lookForSpecificGetterMethod(config, "sender"));
    }

    private static Method lookForReceiverGetterMethod(final DisApi config) {
        return ofNullable(lookForSpecificGetterMethod(config, "getReceiver"))
                .orElse(lookForSpecificGetterMethod(config, "receiver"));
    }

    private static Resource extractResourceOfMethod(final Method method, final DisApi config) {
        if (null != method && Resource.class.isAssignableFrom(method.getReturnType())) {
            try {
                return (Resource) method.invoke(config);
            } catch (final IllegalAccessException | InvocationTargetException ignore) { }
        }
        return null;
    }

    private static Method lookForSpecificSetterMethod(final DisApi config, final String name) {
        try {
            return config.getClass().getMethod(name, Resource.class);
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }

    private static Method lookForSpecificGetterMethod(final DisApi config, String name) {
        try {
            return config.getClass().getMethod(name);
        } catch (final NoSuchMethodException ignored) {
            return null;
        }
    }
}