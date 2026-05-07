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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.MessageFactory;

public interface LoggerContext {

    Object getExternalContext();

    default ExtendedLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    default ExtendedLogger getLogger(Class<?> clazz, MessageFactory messageFactory) {
        return getLogger(clazz.getName());
    }

    ExtendedLogger getLogger(String name);

    ExtendedLogger getLogger(String name, MessageFactory messageFactory);

    default LoggerRegistry<? extends Logger> getLoggerRegistry() {
        return null;
    }

    default Object getObject(String key) {
        return null;
    }

    boolean hasLogger(String name);

    boolean hasLogger(String name, Class<? extends MessageFactory> messageFactoryClass);

    boolean hasLogger(String name, MessageFactory messageFactory);

    default Object putObject(String key, Object value) {
        return null;
    }

    default Object putObjectIfAbsent(String key, Object value) {
        return null;
    }

    default Object removeObject(String key) {
        return null;
    }

    default boolean removeObject(String key, Object value) {
        return false;
    }
}