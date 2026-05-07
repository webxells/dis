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

import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.config.description.Internal;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.ForEachObject;
import com.webxells.dis.info.internal.clazz.option.ImplementationOf;
import com.webxells.dis.info.internal.clazz.option.Primitive;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class Implementation {
    public class OptionReader implements ForEachObject<Option> {
        @Override
        public void forEachImplementation(final Consumer<Option> consumer) {
            try {
                assertOptionsAreLoaded();
            } catch (final InputOutputError e) {
                throw new RuntimeException("could not load implementations", e);
            }
            options.values().forEach(consumer);
        }
    }

    private final Class<?> clazz;
    private final Class<?> configurationClass;
    private final DisImplementationManager disImplementationManager;

    private Map<String, Option> options;

    public Implementation(final Interface forInterface, final Class<?> clazz, final DisImplementationManager disImplementationManager) {
        this.clazz = clazz;
        configurationClass = getConfigurationClass(clazz, forInterface);
        this.disImplementationManager = disImplementationManager;
    }

    public static List<Field> getFields(final Class<?> clazz) {
        final List<Field> result = new ArrayList<>(Arrays.asList(clazz.getDeclaredFields()));
        if (clazz.getSuperclass() != null) {
            result.addAll(getFields(clazz.getSuperclass()));
        }
        return result;
    }

    public OptionReader readOptions() {
        return new OptionReader();
    }

    public <T extends Annotation> Optional<T> getAnnotation(final Class<T> annotationClass) {
        final T classAnnotation = clazz.getAnnotation(annotationClass);
        return null == classAnnotation ? Optional.ofNullable(configurationClass.getAnnotation(annotationClass)) :
                Optional.of(classAnnotation);
    }

    public String getName() {
        return clazz.getName();
    }

    private Class<?> getConfigurationClass(final Class<?> clazz, final Interface forInterface) {
        final Class<?> result = Optional.of(clazz)
                .filter(a -> forInterface.hasExternConfiguration())
                .map(this::searchForGeneric)
                .orElse(null);
        return null == result ? clazz : result;
    }

    private Class<?> searchForGeneric(final Class<?> clazz) {
        final Class<?> interfaceResult = searchInClassesForGeneric(clazz.getGenericInterfaces());
        if (null != interfaceResult) {
            return interfaceResult;
        }
        final Class<?> superClassResult = searchInClassesForGeneric(new Type[] {clazz.getGenericSuperclass()});
        if (null != superClassResult) {
            return superClassResult;
        }
        if (clazz.getSuperclass() != null) {
            return searchForGeneric(clazz.getSuperclass());
        }
        return null;
    }

    private Class<?> searchInClassesForGeneric(final Type[] types) {
        if (types.length == 1 && types[0] instanceof ParameterizedType genericInterface) {
            final Type[] actualTypeArguments = genericInterface.getActualTypeArguments();
            if (actualTypeArguments.length == 1 && actualTypeArguments[0] instanceof Class<?> result) {
                return result;
            }
        }
        return null;
    }

    private void assertOptionsAreLoaded() throws InputOutputError {
        if (null == options) {
            try {
                options = new HashMap<>();
                Arrays.stream(configurationClass.getMethods())
                        .filter(a -> Object.class != a.getDeclaringClass())
                        .filter(this::isValidOptionMethod)
                        .map(method -> new Option(method, configurationClass))
                        .forEach(this::verify);
                getFields(configurationClass).stream()
                        .filter(a -> Object.class != a.getDeclaringClass())
                        .filter(this::isValidOptionField)
                        .map(Option::new)
                        .forEach(this::verify);
            }
            catch (final Exception e) {
                throw new InputOutputError("could not load options", e);
            }
        }
    }

    private void verify(Option option) {
        if (option.getType() instanceof ImplementationOf implementationType) {
            if (isDisImplementation(implementationType.getImplementationClass())) {
                disImplementationManager.saveForLater(implementationType.getClassName());
                options.putIfAbsent(option.getName(), option);
            }
        } else {
            options.putIfAbsent(option.getName(), option);
        }
    }

    private boolean isDisImplementation(final Class<?> parameter) {
        return Enum.class.isAssignableFrom(parameter) || Primitive.isAssignable(parameter) ||
                DisApi.class.isAssignableFrom(parameter) || DisApi.class.isAssignableFrom(parameter.getNestHost());
    }

    private boolean isValidOptionField(final Field field) {
        final int modifiers = field.getModifiers();
        final int classModifiers = field.getDeclaringClass().getModifiers();
        return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) &&
                !Modifier.isNative(classModifiers) && !Modifier.isAbstract(classModifiers);
    }

    private boolean isValidOptionMethod(final Method method) {
        final String name = method.getName();
        final int modifiers = method.getModifiers();
        return 1 == method.getParameterCount() && Object.class != method.getParameterTypes()[0] && name.startsWith("set") && name.length() > 3 &&
                Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers) &&
                !Modifier.isAbstract(modifiers) && annotationNotFound(method, Internal.class) &&
                annotationNotFound(method, Deprecated.class);
    }

    private <T extends Annotation> boolean annotationNotFound(final Method method, final Class<T> annotation) {
        return method.getAnnotationsByType(annotation).length == 0;
    }
}