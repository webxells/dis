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
package com.webxells.dis.test.cases;

import com.webxells.dis.api.DatasetPiece;
import com.webxells.dis.api.config.MappingPoint;
import com.webxells.dis.base.SimpleDatasetPiece;
import com.webxells.dis.base.config.SimpleMappingPoint;
import com.webxells.dis.boot.WorkflowSecurity;
import com.webxells.dis.logging.LoggerProxyFactory;
import com.webxells.dis.test.TestLogManager;
import java.io.File;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.opentest4j.AssertionFailedError;

public class SimpleTestCase {
    private static final Set<String> UNIQUE = new HashSet<>();
    private static final TestLogManager TEST_LOG_MANAGER = new TestLogManager();
    private boolean workflowSecurityEnabled;

    @BeforeAll
    static void setUpLogger() {
        LoggerProxyFactory.registerLogManager(TEST_LOG_MANAGER);
    }

    @BeforeEach
    public void setUpSimpleTestCase() {
        TEST_LOG_MANAGER.clear();
        UNIQUE.clear();
    }

    @AfterEach
    public void clearWorkflowSecurity() {
        if (workflowSecurityEnabled) {
            TestWorkflowSecurity.clear();
            workflowSecurityEnabled = false;
        }
    }

    public void setUpWorkflowSecurity() {
        if (!WorkflowSecurity.isSetUp()) {
            WorkflowSecurity.register(new TestWorkflowSecurity());
            workflowSecurityEnabled = true;
        }
    }

    public static TestLogManager.TestLogger getLogger(String name) {
        return TEST_LOG_MANAGER.get(name);
    }

    public static TestLogManager.TestLogger getLogger(Class<?> clazz) {
        return getLogger(clazz.getName());
    }

    public static ConfigurationBuilder newConfiguration() {
        return new ConfigurationBuilder();
    }

    public static DatasetPiece createDataSetPiece() {
        return new SimpleDatasetPiece(random());
    }

    public static DatasetPiece createDataSetPiece(String content) {
        return new SimpleDatasetPiece(content);
    }

    public static MappingPoint createMappingPoint() {
        return new SimpleMappingPoint(random(), random());
    }

    public static MappingPoint createMappingPoint(String reference) {
        return new SimpleMappingPoint(reference, random());
    }

    public static MappingPoint createMappingPoint(String reference, String path) {
        return new SimpleMappingPoint(reference, path);
    }

    public static String random(final String description) {
        return String.format("_%s[%s]_", random(), description);
    }

    public static String randomUnique() {
        String value;
        do {
            value = random();
        } while (UNIQUE.contains(value));
        UNIQUE.add(value);
        return value;
    }

    public static int random(int setIntToReturnInt) {
        return randomMax(Integer.MAX_VALUE);
    }

    public static int randomMax(int max) {
        return Math.toIntExact(Math.round(Math.random() * max));
    }

    public static float random(float setFloatToReturnFloat) {
        return randomMax(Float.MAX_VALUE);
    }

    public static float randomMax(float max) {
        return (float) Math.random() * max;
    }

    public static double random(double setDoubleToReturnDouble) {
        return randomMax(Double.MAX_VALUE);
    }

    public static double randomMax(double max) {
        return Math.random() * max;
    }

    public static long random(long setLongToReturnLong) {
        return randomMax(Long.MAX_VALUE);
    }

    public static long randomMax(long max) {
        return Math.round(Math.random() * max);
    }

    public static boolean random(boolean setBoolToReturnBool) {
        return 0 == Math.round(Math.random() * 665) % 2;
    }

    public static String random() {
        return String.valueOf(random(0));
    }

    public static <T, R> Map<T, R> linkedMapOf(T key, R value) {
        LinkedHashMap<T, R> result = new LinkedHashMap<>();
        result.put(key, value);
        return Collections.unmodifiableMap(result);
    }

    public static <T, R> Map<T, R> linkedMapOf(T key1, R value1,
                                         T key2, R value2) {
        LinkedHashMap<T, R> result = new LinkedHashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        return Collections.unmodifiableMap(result);
    }

    public static <T, R> Map<T, R> linkedMapOf(T key1, R value1,
                                         T key2, R value2,
                                         T key3, R value3) {
        LinkedHashMap<T, R> result = new LinkedHashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        return Collections.unmodifiableMap(result);
    }

    public static <T, R> Map<T, R> linkedMapOf(T key1, R value1,
                                         T key2, R value2,
                                         T key3, R value3,
                                         T key4, R value4) {
        LinkedHashMap<T, R> result = new LinkedHashMap<>();
        result.put(key1, value1);
        result.put(key2, value2);
        result.put(key3, value3);
        result.put(key4, value4);
        return Collections.unmodifiableMap(result);
    }

    public static InputStream getResourceFileStream(final String file) {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
    }

    public static File getResourceFile(final String file) throws URISyntaxException {
        URL resource = Thread.currentThread().getContextClassLoader().getResource(file);
        if (null == resource) {
            throw new AssertionFailedError("resource not found: " + file);
        }
        return new File(resource.toURI());
    }

    public static void allTrue(final Boolean... values) {
        Assertions.assertTrue(
                Arrays.stream(values).allMatch(Boolean::booleanValue),
                () -> "Not all true: " + Arrays.toString(values));
    }

    public static void allFalse(final Boolean... values) {
        Assertions.assertFalse(
                Arrays.stream(values).allMatch(Boolean::booleanValue),
                () -> "Not all true: " + Arrays.toString(values));
    }

}