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
package org.apache.commons.logging;

import java.lang.reflect.Constructor;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LogSource {
    private static final LogFactory LOG_FACTORY = LogFactory.getFactory();
    private static final Map<String, Log> REAL_LOGS = new ConcurrentHashMap<>();

    protected static Hashtable<String, Log> logs = new Hashtable<>(1, 1);
    protected static boolean log4jIsAvailable = true;
    protected static boolean jdk14IsAvailable = true;
    protected static Constructor<?> logImplctor;

    static {
        try {
            logImplctor = DisLogger.class.getConstructor(Object.class);
        } catch (final NoSuchMethodException e) {
            throw new RuntimeException("unable to get constructor", e);
        }
    }

    public static Log getInstance(final Class<?> clazz) {
        return getInstance(clazz.getName());
    }

    public static Log getInstance(final String name) {
        return REAL_LOGS.computeIfAbsent(name, LogSource::createLog);
    }

    public static String[] getLogNames() {
        return REAL_LOGS.keySet().toArray(new String[0]);
    }

    public static Log makeNewLogInstance(final String name) {
        return REAL_LOGS.compute(name, (a, b) -> createLog(a));
    }

    public static void setLogImplementation(final Class<?> logClass) { }

    public static void setLogImplementation(final String className) { }

    private static Log createLog(final String name) {
        return LOG_FACTORY.getInstance(name);
    }
}