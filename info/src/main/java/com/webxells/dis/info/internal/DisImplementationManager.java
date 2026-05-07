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

import com.webxells.dis.api.config.DisApi;
import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.info.internal.clazz.Interface;
import com.webxells.dis.info.internal.clazz.option.Primitive;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class DisImplementationManager {
    private static final ClassDiscoverer CLASS_DISCOVERER = new ClassDiscoverer();
    private final Queue<String> requiredClasses = new LinkedList<>();
    private final Set<String> workedClasses = new HashSet<>();
    private final List<String> interfaces;

    public DisImplementationManager(final List<String> interfaces) {
        this.interfaces = interfaces;
    }

    public void forEachInterface(final Consumer<Interface> consumer) throws InputOutputError {
        workedClasses.clear();
        CLASS_DISCOVERER.stream()
                .filter(DisApi.class::isAssignableFrom)
                .filter(a -> interfaces.contains(a.getName()))
                .map(clazz -> new Interface(clazz, this))
                .forEach(a -> callConsumer(a, consumer));
        while (!requiredClasses.isEmpty()) {
            final String current = requiredClasses.poll();
            if (!workedClasses.contains(current)) {
                Optional.ofNullable(ClassDiscoverer.loadClassByName(current))
                        .filter(a -> !Primitive.isAssignable(a) && Object.class != a)
                        .map(a -> new Interface(a, this))
                        .ifPresentOrElse(a -> callConsumer(a, consumer), () -> workedClasses.add(current));
            }
        }
    }

    private void callConsumer(final Interface current, final Consumer<Interface> consumer) {
        workedClasses.add(current.getName());
        consumer.accept(current);
    }

    public Stream<Class<?>> classes() throws InputOutputError {
        return CLASS_DISCOVERER.stream();
    }

    public void saveForLater(final String clazz) {
        if (!(workedClasses.contains(clazz) || requiredClasses.contains(clazz))) {
            requiredClasses.add(clazz);
        }
    }
}