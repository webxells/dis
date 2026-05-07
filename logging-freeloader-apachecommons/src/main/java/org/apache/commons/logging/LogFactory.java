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

import com.webxells.dis.api.Logger;
import com.webxells.dis.logging.LoggerProxyFactory;
import java.util.Hashtable;
import java.util.Optional;

public class LogFactory {
    public static final String PRIORITY_KEY = "priority";
    public static final String TCCL_KEY = "use_tccl";
    public static final String FACTORY_PROPERTY = "org.apache.commons.logging.LogFactory";
    public static final String FACTORY_DEFAULT = "org.apache.commons.logging.LogFactory";
    public static final String FACTORY_PROPERTIES = "commons-logging.properties";
    public static final String DIAGNOSTICS_DEST_PROPERTY = "org.apache.commons.logging.diagnostics.dest";
    public static final String HASHTABLE_IMPLEMENTATION_PROPERTY = "org.apache.commons.logging.LogFactory.HashtableImpl";
    protected static final String SERVICE_ID = "META-INF/services/org.apache.commons.logging.LogFactory";
    private static final Logger LOGGER = LoggerProxyFactory.logger("C.diagnose");
    private static final LogFactory INSTANCE = new LogFactory();

    protected static Hashtable<ClassLoader, LogFactory> factories = new Hashtable<>(1, 2) {{
        put(LogFactory.class.getClassLoader(), INSTANCE);
    }};


    protected static Object createFactory(final String factoryClassName, final ClassLoader classLoader) {
        return INSTANCE;
    }

    protected static ClassLoader directGetContextClassLoader() throws LogConfigurationException {
        return LogFactory.class.getClassLoader();
    }

    protected static ClassLoader getClassLoader(final Class<?> clazz) {
        return clazz.getClassLoader();
    }

    public static LogFactory getFactory() throws LogConfigurationException {
        return INSTANCE;
    }

    public static Log getLog(final Class<?> clazz) throws LogConfigurationException {
        return INSTANCE.getInstance(clazz);
    }

    public static Log getLog(final String name) throws LogConfigurationException {
        return INSTANCE.getInstance(name);
    }
    protected static void handleThrowable(final Throwable t) { }
    protected static boolean isDiagnosticsEnabled() {
        return Logger.LogLevel.TRACE == LOGGER.getLevel();
    }

    protected static void logRawDiagnostic(final String msg) {
        LOGGER.t(msg);
    }
    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader) {
        return INSTANCE;
    }

    protected static LogFactory newFactory(final String factoryClass,
                                           final ClassLoader classLoader,
                                           final ClassLoader contextClassLoader) {
        return INSTANCE;
    }

    public static String objectId(final Object o) {
        return Optional.ofNullable(o)
                .map(Object::toString)
                .orElse("null");
    }

    public static void release(final ClassLoader classLoader) {  }


    public static void releaseAll() {  }

    protected LogFactory() {  }

    public Log getInstance(Class<?> clazz) throws LogConfigurationException {
        return getInstance(clazz.getName());
    }

    public Log getInstance(String name) throws LogConfigurationException {
        return new DisLogger(name);
    }

    public void setAttribute(String name, Object value) { }

    public void removeAttribute(String name) { }

    public void release() { }

    public Object getAttribute(String name) { return name; }

    public  String[] getAttributeNames() { return new String[] {}; }

}