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
package com.webxells.dis.test.example.input;

import com.webxells.dis.api.config.InputConfig;
import com.webxells.dis.api.resource.Resource;
import java.util.List;
import java.util.Map;

public class TestInputConfig implements InputConfig {
    private String name;
    private String type = TestInput.class.getName();
    private NestedTestClass nestedTestClass;
    private Resource receiver;
    private String testInputField;
    private Map<String, String> stringMap;
    private Map<String, NestedTestClass> stringObjectMap;
    private Map<String, Integer> stringIntMap;
    private Map<String, Boolean> stringBooleanMap;
    private Map<String, List<String>> stringListStringMap;
    private Map<String, List<NestedTestClass>> stringListObjectMap;
    private List<NestedTestClass> nestedImplementations;
    private boolean shouldWaitForSomeTestAssertions;
    private boolean shouldTriggerException;

    public TestInputConfig() {}

    public TestInputConfig(final String name) {
        this.name = name;
    }

    public TestInputConfig(final String name, final String type) {
        this(name);
        this.type = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getType() {
        return type;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public NestedTestClass getNestedTestClass() {
        return nestedTestClass;
    }

    public void setNestedTestClass(final NestedTestClass nestedTestClass) {
        this.nestedTestClass = nestedTestClass;
    }

    public Resource getReceiver() {
        return receiver;
    }

    public void setReceiver(final Resource receiver) {
        this.receiver = receiver;
    }

    public String getTestInputField() {
        return testInputField;
    }

    public void setTestInputField(final String testInputField) {
        this.testInputField = testInputField;
    }

    public Map<String, String> getStringMap() {
        return stringMap;
    }

    public void setStringMap(final Map<String, String> stringMap) {
        this.stringMap = stringMap;
    }

    public Map<String, NestedTestClass> getStringObjectMap() {
        return stringObjectMap;
    }

    public void setStringObjectMap(final Map<String, NestedTestClass> stringObjectMap) {
        this.stringObjectMap = stringObjectMap;
    }

    public Map<String, Integer> getStringIntMap() {
        return stringIntMap;
    }

    public void setStringIntMap(final Map<String, Integer> stringIntMap) {
        this.stringIntMap = stringIntMap;
    }

    public Map<String, Boolean> getStringBooleanMap() {
        return stringBooleanMap;
    }

    public void setStringBooleanMap(final Map<String, Boolean> stringBooleanMap) {
        this.stringBooleanMap = stringBooleanMap;
    }

    public Map<String, List<String>> getStringListStringMap() {
        return stringListStringMap;
    }

    public void setStringListStringMap(final Map<String, List<String>> stringListStringMap) {
        this.stringListStringMap = stringListStringMap;
    }

    public Map<String, List<NestedTestClass>> getStringListObjectMap() {
        return stringListObjectMap;
    }

    public void setStringListObjectMap(final Map<String, List<NestedTestClass>> stringListObjectMap) {
        this.stringListObjectMap = stringListObjectMap;
    }

    public List<NestedTestClass> getNestedImplementations() {
        return nestedImplementations;
    }

    public void setNestedImplementations(final List<NestedTestClass> nestedImplementations) {
        this.nestedImplementations = nestedImplementations;
    }

    public boolean isShouldWaitForSomeTestAssertions() {
        return shouldWaitForSomeTestAssertions;
    }

    public void setShouldWaitForSomeTestAssertions(final boolean shouldWaitForSomeTestAssertions) {
        this.shouldWaitForSomeTestAssertions = shouldWaitForSomeTestAssertions;
    }

    public boolean isShouldTriggerException() {
        return shouldTriggerException;
    }

    public void setShouldTriggerException(final boolean shouldTriggerException) {
        this.shouldTriggerException = shouldTriggerException;
    }
}