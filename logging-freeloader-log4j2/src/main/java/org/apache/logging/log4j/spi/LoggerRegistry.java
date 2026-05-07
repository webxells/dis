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
package org.apache.logging.log4j.spi;

import java.util.Collection;
import java.util.Map;
import org.apache.logging.log4j.message.MessageFactory;

public class LoggerRegistry<T extends ExtendedLogger> {
    public LoggerRegistry() {
        this(null);
    }

    public LoggerRegistry(final Object factory) {
    }

    private static String factoryClassKey(final Class<? extends MessageFactory> messageFactoryClass) {
        return null;
    }

    private static String factoryKey(final MessageFactory messageFactory) {
        return null;
    }

    public T getLogger(final String name) {
        return null;
    }

    public T getLogger(final String name, final MessageFactory messageFactory) {
        return null;
    }

    public Collection<T> getLoggers() {
        return null;
    }

    public Collection<T> getLoggers(final Collection<T> destination) {
        return null;
    }

    private Map<String, T> getOrCreateInnerMap(final String factoryName) {
        return null;
    }

    public boolean hasLogger(final String name) {
        return false;
    }

    public boolean hasLogger(final String name, final MessageFactory messageFactory) {
        return false;
    }

    public boolean hasLogger(final String name, final Class<? extends MessageFactory> messageFactoryClass) {
        return false;
    }

    public void putIfAbsent(final String name, final MessageFactory messageFactory, final T logger) {
    }

    public static class WeakMapFactory<T extends ExtendedLogger> implements MapFactory<T> {
    }

    public static class ConcurrentMapFactory<T extends ExtendedLogger> implements MapFactory<T> {
    }

    public interface MapFactory<T extends ExtendedLogger> {
        default Map<String, T> createInnerMap() {
            return null;
        }

        default Map<String, Map<String, T>> createOuterMap() {
            return null;
        }

        default void putIfAbsent(Map<String, T> innerMap, String name, T logger) {
        }
    }
}