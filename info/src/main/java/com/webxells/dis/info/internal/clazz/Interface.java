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

import com.webxells.dis.api.error.InputOutputError;
import com.webxells.dis.api.input.Input;
import com.webxells.dis.api.output.Output;
import com.webxells.dis.api.trigger.Trigger;
import com.webxells.dis.info.internal.DisImplementationManager;
import com.webxells.dis.info.internal.ForEachObject;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Interface implements ForEachObject<Implementation> {
    private final Class<?> clazz;
    private final DisImplementationManager disImplementationManager;

    private List<Implementation> implementations;

    public Interface(final Class<?> clazz, final DisImplementationManager disImplementationManager) {
        this.clazz = clazz;
        this.disImplementationManager = disImplementationManager;
    }

    public String getName() {
        return clazz.getName();
    }


    @Override
    public void forEachImplementation(final Consumer<Implementation> consumer) {
        try {
            assertImplementationsLoaded();
        } catch (final InputOutputError e) {
            throw new RuntimeException("could not load implementations", e);
        }
        implementations.forEach(consumer);
    }

    public boolean hasExternConfiguration() {
        return Stream.of(Trigger.class, Input.class, Output.class)
                .anyMatch(a -> a.isAssignableFrom(clazz));
    }

    private void assertImplementationsLoaded() throws InputOutputError {
        if (null == implementations) {
            implementations = disImplementationManager.classes()
                    .filter(this::isValidImplementation)
                    .map(a -> new Implementation(this, a, disImplementationManager))
                    .toList();
        }
    }

    private boolean isValidImplementation(final Class<?> other) {
        final int modifiers = other.getModifiers();
        return clazz.isAssignableFrom(other) &&
                !(Modifier.isAbstract(modifiers) || Modifier.isInterface(modifiers));
    }
}