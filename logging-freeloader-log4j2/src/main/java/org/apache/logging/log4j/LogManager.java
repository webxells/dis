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
package org.apache.logging.log4j;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.logging.log4j.spi.LoggerContext;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContextFactory;

public class LogManager {
    public static class Context implements LoggerContext {
        private final Map<String, ExtendedLogger> registry = new ConcurrentHashMap<>();

        @Override
        public Object getExternalContext() {
            return null;
        }

        @Override
        public ExtendedLogger getLogger(final String name) {
            return registry.computeIfAbsent(name, DisLogger4Log4J::new);
        }

        @Override
        public ExtendedLogger getLogger(final String name, final MessageFactory messageFactory) {
            return getLogger(name);
        }

        @Override
        public boolean hasLogger(final String name) {
            return registry.containsKey(name);
        }

        @Override
        public boolean hasLogger(final String name, final Class<? extends MessageFactory> messageFactoryClass) {
            return hasLogger(name);
        }

        @Override
        public boolean hasLogger(final String name, final MessageFactory messageFactory) {
            return hasLogger(name);
        }
    }

    public static final String FACTORY_PROPERTY_NAME = "dis-puppet";
    public static final String ROOT_LOGGER_NAME = "";
    public static final LoggerContext CONTEXT = new Context();
    public static final LoggerContextFactory FACTORY = new LoggerContextFactory() { };

    protected LogManager() {  }

    public static boolean exists(final String name) {
        return getContext().hasLogger(name);
    }

    public static LoggerContext getContext() {
        return CONTEXT;
    }

    public static LoggerContext getContext(final boolean currentContext) {
        return CONTEXT;
    }

    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext) {
        return CONTEXT;
    }

    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext) {
        return CONTEXT;
    }

    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final URI configLocation) {
        return CONTEXT;
    }

    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext, final URI configLocation) {
        return CONTEXT;
    }

    public static LoggerContext getContext(final ClassLoader loader, final boolean currentContext, final Object externalContext, final URI configLocation, final String name) {
        return CONTEXT;
    }

    protected static LoggerContext getContext(final String fqcn, final boolean currentContext) {
        return CONTEXT;
    }

    protected static LoggerContext getContext(final String fqcn, final ClassLoader loader, final boolean currentContext) {
        return CONTEXT;
    }

    protected static LoggerContext getContext(final String fqcn, final ClassLoader loader, final boolean currentContext, final URI configLocation, final String name) {
        return CONTEXT;
    }

    public static void shutdown() { }

    public static void shutdown(final boolean currentContext) { }

    public static void shutdown(final boolean currentContext, final boolean allContexts) { }

    public static void shutdown(final LoggerContext context) { }

    public static LoggerContextFactory getFactory() {
        return FACTORY;
    }

    public static void setFactory(final LoggerContextFactory factory) { }

    public static Logger getLogger() {
        return getLogger(Arrays.stream(Thread.currentThread().getStackTrace())
                .filter(a -> !LogManager.class.getName().equals(a.getClassName()))
                .findFirst()
                    .map(StackTraceElement::getClassName)
                    .orElse("unknown"));
    }

    public static Logger getLogger(final String name) {
        return CONTEXT.getLogger(name);
    }

    public static Logger getLogger(final Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static Logger getFormatterLogger() {
        return getLogger();
    }

    public static Logger getFormatterLogger(final Class<?> clazz) {
        return getLogger(clazz);
    }

    public static Logger getFormatterLogger(final Object value) {
        return getLogger(toString(value));
    }

    private static String toString(final Object value) {
        return Optional.ofNullable(value)
                .map(Object::toString)
                .orElse("null");
    }

    public static Logger getFormatterLogger(final String name) {
        return getLogger(name);
    }

    public static Logger getLogger(final Class<?> clazz, final MessageFactory messageFactory) {
        return getLogger(clazz);
    }

    public static Logger getLogger(final MessageFactory messageFactory) {
        return getLogger();
    }

    public static Logger getLogger(final Object value) {
        return getLogger(toString(value));
    }

    public static Logger getLogger(final Object value, final MessageFactory messageFactory) {
        return getLogger(toString(value));
    }

    public static Logger getLogger(final String name, final MessageFactory messageFactory) {
        return getLogger(name);
    }

    protected static Logger getLogger(final String fqcn, final String name) {
        return getLogger(name);
    }

    public static Logger getRootLogger() {
        return getLogger(ROOT_LOGGER_NAME);
    }

}